package com._Blog.mojebbari.services;

import com._Blog.mojebbari.dto.LikeResponse;
import com._Blog.mojebbari.models.Like;
import com._Blog.mojebbari.models.Post;
import com._Blog.mojebbari.models.User;
import com._Blog.mojebbari.repositories.LikeRepository;
import com._Blog.mojebbari.repositories.PostRepository;
import com._Blog.mojebbari.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing post likes
 * 
 * Business logic for:
 * - Like a post
 * - Unlike a post
 * - Get users who liked a post
 * - Check if user liked a post
 * 
 * @Transactional: All database operations run in a transaction
 * - If something fails, all changes are rolled back
 * - Ensures data consistency
 */
@Service
@RequiredArgsConstructor
@Transactional
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /**
     * Like a post
     * 
     * Business Rules:
     * 1. User must exist
     * 2. Post must exist
     * 3. User can't like the same post twice
     * 
     * @param postId - ID of post to like
     * @param userEmail - Email of user who wants to like
     * @throws EntityNotFoundException if user or post not found
     * @throws IllegalStateException if already liked
     */
    public void likePost(Long postId, String userEmail) {
        // 1. Find user by email or username
        User user = userRepository.findByEmailOrUsername(userEmail, userEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userEmail));

        // 2. Find post
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with ID: " + postId));

        // 3. Check if already liked
        if (likeRepository.existsByUserAndPost(user, post)) {
            throw new IllegalStateException("You have already liked this post");
        }

        // 4. Create like
        Like like = Like.builder()
                .user(user)
                .post(post)
                .build();

        // 5. Save to database
        likeRepository.save(like);

        // 6. Create notification for post author
        notificationService.createLikeNotification(user, post);
    }

    /**
     * Unlike a post
     * 
     * Business Rules:
     * 1. User must exist
     * 2. Post must exist
     * 3. User must have previously liked the post
     * 
     * @param postId - ID of post to unlike
     * @param userEmail - Email of user who wants to unlike
     * @throws EntityNotFoundException if like doesn't exist
     */
    public void unlikePost(Long postId, String userEmail) {
        // 1. Find user by email or username
        User user = userRepository.findByEmailOrUsername(userEmail, userEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userEmail));

        // 2. Find post
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with ID: " + postId));

        // 3. Find the like record
        Like like = likeRepository.findByUserAndPost(user, post)
                .orElseThrow(() -> new EntityNotFoundException("You have not liked this post"));

        // 4. Delete the like
        likeRepository.delete(like);
    }

    /**
     * Get list of users who liked a post
     * 
     * @param postId - ID of the post
     * @return List of LikeResponse (user info + when they liked)
     */
    public List<LikeResponse> getLikesByPost(Long postId) {
        // 1. Find post
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with ID: " + postId));

        // 2. Get all likes for this post
        List<Like> likes = likeRepository.findByPostOrderByCreatedAtDesc(post);

        // 3. Convert to DTOs
        return likes.stream()
                .map(this::mapToLikeResponse)
                .collect(Collectors.toList());
    }

    /**
     * Check if a user liked a specific post (by email)
     * 
     * @param postId - ID of the post
     * @param userEmail - Email of the user
     * @return true if user liked the post, false otherwise
     */
    public boolean isPostLikedByUser(Long postId, String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElse(null);
        Post post = postRepository.findById(postId).orElse(null);

        if (user == null || post == null) {
            return false;
        }

        return likeRepository.existsByUserAndPost(user, post);
    }

    /**
     * Get like count for a post
     * 
     * @param postId - ID of the post
     * @return Number of likes
     */
    public long getLikeCount(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with ID: " + postId));

        return likeRepository.countByPost(post);
    }

    /**
     * Helper method: Convert Like entity to LikeResponse DTO
     * 
     * @param like - Like entity from database
     * @return LikeResponse DTO for API
     */
    private LikeResponse mapToLikeResponse(Like like) {
        User user = like.getUser();
        
        return LikeResponse.builder()
                .userId(user.getId())
                .username(user.getUsername()) // Use actual username, not email
                .profilePicture(user.getProfilePicture())
                .likedAt(like.getCreatedAt())
                .build();
    }
}
