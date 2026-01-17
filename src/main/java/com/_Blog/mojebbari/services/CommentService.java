package com._Blog.mojebbari.services;

import com._Blog.mojebbari.dto.CommentRequest;
import com._Blog.mojebbari.dto.CommentResponse;
import com._Blog.mojebbari.models.Comment;
import com._Blog.mojebbari.models.Post;
import com._Blog.mojebbari.models.Role;
import com._Blog.mojebbari.models.User;
import com._Blog.mojebbari.repositories.CommentRepository;
import com._Blog.mojebbari.repositories.PostRepository;
import com._Blog.mojebbari.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing post comments
 * 
 * Business logic for:
 * - Create comment
 * - Get comments for a post
 * - Update own comment
 * - Delete own comment (or admin deletes any)
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /**
     * Add a comment to a post
     * 
     * Business Rules:
     * 1. User must exist
     * 2. Post must exist
     * 3. Comment text must not be empty (validated by DTO)
     * 
     * @param postId - ID of post to comment on
     * @param userEmail - Email of user creating comment
     * @param request - Comment data (text)
     * @return CommentResponse with created comment details
     */
    public CommentResponse addComment(Long postId, String userEmail, CommentRequest request) {
        // 1. Find user by email or username
        User user = userRepository.findByEmailOrUsername(userEmail, userEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userEmail));

        // 2. Find post
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with ID: " + postId));

        // 3. Create comment
        Comment comment = Comment.builder()
                .text(request.getText())
                .user(user)
                .post(post)
                .build();

        // 4. Save to database
        Comment savedComment = commentRepository.save(comment);

        // 5. Create notification for post author
        notificationService.createCommentNotification(user, post, savedComment);

        // 6. Convert to DTO and return
        return mapToCommentResponse(savedComment, user.getId());
    }

    /**
     * Get all comments for a post
     * 
     * Sorted chronologically (newest first for better UX)
     * 
     * @param postId - ID of the post
     * @return List of CommentResponse
     */
    public List<CommentResponse> getCommentsByPost(Long postId) {
        // 1. Find post
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with ID: " + postId));

        // 2. Get all comments (newest first)
        List<Comment> comments = commentRepository.findByPostOrderByCreatedAtDesc(post);

        // 3. Convert to DTOs
        return comments.stream()
                .map(comment -> mapToCommentResponse(comment, null))
                .collect(Collectors.toList());
    }

    /**
     * Update a comment
     * 
     * Business Rules:
     * 1. Comment must exist
     * 2. Only comment author can update
     * 3. Cannot update other users' comments (not even admin)
     * 
     * @param commentId - ID of comment to update
     * @param userEmail - Email of user trying to update
     * @param request - New comment data
     * @return Updated CommentResponse
     * @throws AccessDeniedException if user is not the author
     */
    public CommentResponse updateComment(Long commentId, String userEmail, CommentRequest request) {
        // 1. Find comment
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with ID: " + commentId));

        // 2. Find user
        User user = userRepository.findByEmailOrUsername(userEmail, userEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userEmail));

        // 3. Authorization: Only author can update
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You can only update your own comments");
        }

        // 4. Update text
        comment.setText(request.getText());

        // 5. Save (updatedAt will be auto-set by @UpdateTimestamp)
        Comment updatedComment = commentRepository.save(comment);

        // 6. Return DTO
        return mapToCommentResponse(updatedComment, user.getId());
    }

    /**
     * Delete a comment
     * 
     * Business Rules:
     * 1. Comment must exist
     * 2. Only comment author OR admin can delete
     * 
     * Authorization:
     * - Author: Can delete own comment
     * - Admin: Can delete any comment (moderation)
     * - Others: Cannot delete
     * 
     * @param commentId - ID of comment to delete
     * @param userEmail - Email of user trying to delete
     * @throws AccessDeniedException if user lacks permission
     */
    public void deleteComment(Long commentId, String userEmail) {
        // 1. Find comment
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with ID: " + commentId));

        // 2. Find current user
        User currentUser = userRepository.findByEmailOrUsername(userEmail, userEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userEmail));

        // 3. Authorization check
        boolean isAuthor = comment.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isAuthor && !isAdmin) {
            throw new AccessDeniedException("You can only delete your own comments");
        }

        // 4. Delete comment
        commentRepository.delete(comment);
    }

    /**
     * Get comment count for a post
     * 
     * @param postId - ID of the post
     * @return Number of comments
     */
    public Long getCommentCount(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with ID: " + postId));

        return commentRepository.countByPost(post);
    }

    // Convert Comment to CommentResponse
    private CommentResponse mapToCommentResponse(Comment comment, Long currentUserId) {
        User author = comment.getUser();
        
        // Check if comment was edited
        boolean isEdited = comment.getUpdatedAt().isAfter(comment.getCreatedAt());
        
        // Check if this is current user's comment
        boolean isOwnComment = currentUserId != null && 
                                currentUserId.equals(author.getId());

        return CommentResponse.builder()
                .id(comment.getId())
                .text(comment.getText())
                .postId(comment.getPost().getId())
                .authorId(author.getId())
                .authorUsername(author.getUsername())
                .authorProfilePicture(author.getProfilePicture())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .isEdited(isEdited)
                .isOwnComment(isOwnComment)
                .build();
    }
}
