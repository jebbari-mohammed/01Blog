package com._Blog.mojebbari.services;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com._Blog.mojebbari.dto.CreatePostRequest;
import com._Blog.mojebbari.dto.PostResponse;
import com._Blog.mojebbari.dto.UpdatePostRequest;
import com._Blog.mojebbari.models.Post;
import com._Blog.mojebbari.models.Role;
import com._Blog.mojebbari.models.User;
import com._Blog.mojebbari.models.Notification;
import com._Blog.mojebbari.repositories.PostRepository;
import com._Blog.mojebbari.repositories.UserRepository;
import com._Blog.mojebbari.repositories.LikeRepository;
import com._Blog.mojebbari.repositories.CommentRepository;
import com._Blog.mojebbari.repositories.SubscriptionRepository;
import com._Blog.mojebbari.repositories.ReportRepository;
import com._Blog.mojebbari.repositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ReportRepository reportRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    // 1. Create a Post
    public PostResponse createPost(CreatePostRequest request, String username) {
        // Find the user who is making the request
        User user = userRepository.findByEmailOrUsername(username, username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Build the post
        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .category(request.getCategory())
                .mediaUrl(request.getMediaUrl())
                .user(user)
                .build();

        // Save to DB
        Post savedPost = postRepository.save(post);

        // Create notifications for all followers
        List<Long> followerIds = subscriptionRepository.findFollowerIdsByFollowingId(user.getId());
        if (!followerIds.isEmpty()) {
            List<User> followers = userRepository.findAllById(followerIds);
            notificationService.createNewPostNotifications(user, savedPost, followers);
        }

        // Convert to DTO to return
        return mapToResponse(savedPost);
    }

    // 2. Get All Posts (For the feed) with pagination
    public Page<PostResponse> getAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    // 2b. Get Posts from Followed Users (Personalized Feed)
    public Page<PostResponse> getFollowedUsersPosts(String username, Pageable pageable) {
        // Find the current user
        User currentUser = userRepository.findByEmailOrUsername(username, username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        // Get IDs of users that current user follows
        List<Long> followingIds = subscriptionRepository.findFollowingIdsByFollowerId(currentUser.getId());
        
        // If user doesn't follow anyone, return empty page
        if (followingIds.isEmpty()) {
            return Page.empty(pageable);
        }
        
        // Get all posts from followed users
        List<Post> allPosts = new ArrayList<>();
        for (Long userId : followingIds) {
            User followedUser = userRepository.findById(userId).orElse(null);
            if (followedUser != null) {
                allPosts.addAll(postRepository.findAllByUser(followedUser));
            }
        }
        
        // Sort by creation date (newest first)
        allPosts.sort((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()));
        
        // Apply pagination manually
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allPosts.size());
        
        List<PostResponse> postResponses = allPosts.subList(start, end).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        return new PageImpl<>(postResponses, pageable, allPosts.size());
    }

    // 3. Get Posts by Specific User (For profile page)
    public List<PostResponse> getPostsByUsername(String username) {
        User user = userRepository.findByEmailOrUsername(username, username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return postRepository.findAllByUser(user).stream() // Using the method we created earlier!
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    // 4. Update a Post
    public PostResponse updatePost(Long postId, UpdatePostRequest request, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with ID: " + postId));

        // AUTHORIZATION LOGIC: Only author can update
        boolean isAuthor = post.getUser().getEmail().equals(username) || 
                           post.getUser().getUsername().equals(username);
        
        if (!isAuthor) {
            throw new AccessDeniedException("You can only update your own posts");
        }
        
        // Update only non-null fields
        if (request.getTitle() != null) {
            post.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            post.setContent(request.getContent());
        }
        if (request.getCategory() != null) {
            post.setCategory(request.getCategory());
        }
        if (request.getMediaUrl() != null) {
            post.setMediaUrl(request.getMediaUrl());
        }
        
        Post savedPost = postRepository.save(post);
        return mapToResponse(savedPost);
    }
    
    // 5. Delete a Post
    public void deletePost(Long postId, String principalUsername) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with ID: " + postId));

        // Find the User who is trying to delete the post
        User principalUser = userRepository.findByEmailOrUsername(principalUsername, principalUsername)
                .orElseThrow(() -> new EntityNotFoundException("Logged-in user not found"));

        // AUTHORIZATION LOGIC: 
        // 1. Check if the current user is the author of the post.
        boolean isAuthor = post.getUser().getEmail().equals(principalUsername) || 
                           post.getUser().getUsername().equals(principalUsername);
        
        // 2. Check if the current user is an Admin.
        boolean isAdmin = principalUser.getRole() == Role.ADMIN;
        
        // If they are neither the author NOR an admin, deny access.
        if (!isAuthor && !isAdmin) {
             throw new AccessDeniedException("You do not have permission to delete this post.");
        }
        
        // Delete all related entities first to avoid foreign key constraint violations
        // 1. Delete all comments on this post
        List<com._Blog.mojebbari.models.Comment> comments = commentRepository.findByPostOrderByCreatedAtAsc(post);
        commentRepository.deleteAll(comments);
        
        // 2. Delete all likes on this post
        List<com._Blog.mojebbari.models.Like> likes = likeRepository.findByPostOrderByCreatedAtDesc(post);
        likeRepository.deleteAll(likes);
        
        // 3. Delete all reports about this post (find by type and contentId)
        List<com._Blog.mojebbari.models.Report> reports = reportRepository.findByReportTypeOrderByCreatedAtDesc(
            com._Blog.mojebbari.models.ReportType.POST
        ).stream()
         .filter(r -> r.getPost() != null && r.getPost().getId().equals(postId))
         .collect(Collectors.toList());
        reportRepository.deleteAll(reports);
        
        // 4. Delete all notifications related to this post
        List<Notification> notifications = notificationRepository.findAll().stream()
            .filter(n -> n.getPost() != null && n.getPost().getId().equals(postId))
            .collect(Collectors.toList());
        notificationRepository.deleteAll(notifications);
        
        // Finally, delete the post itself
        postRepository.delete(post);
    }

    // Helper method to convert Post Entity -> PostResponse DTO
    private PostResponse mapToResponse(Post post) {
        // Get current logged-in user (if any)
        User currentUser = getCurrentUser();
        System.out.println("DEBUG - MapToResponse for post #" + post.getId() + ", currentUser: " + 
                          (currentUser != null ? currentUser.getUsername() + " (ID: " + currentUser.getId() + ")" : "null"));
        
        // Check if current user has liked this post
        boolean isLiked = false;
        if (currentUser != null) {
            System.out.println("DEBUG - Checking likes for user ID: " + currentUser.getId() + ", post ID: " + post.getId());
            isLiked = likeRepository.existsByUserAndPost(currentUser, post);
            System.out.println("DEBUG - existsByUserAndPost result: " + isLiked);
            
            // Also check total like count for this post
            long likeCount = likeRepository.countByPost(post);
            System.out.println("DEBUG - Total likes for post #" + post.getId() + ": " + likeCount);
        }
        
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .category(post.getCategory())
                .mediaUrl(post.getMediaUrl())
                .createdAt(post.getCreatedAt())
                .authorUsername(post.getUser().getUsername()) // Use actual username, not email
                .authorId(post.getUser().getId())
                .likeCount(likeRepository.countByPost(post))
                .commentCount(commentRepository.countByPost(post))
                .isLikedByCurrentUser(isLiked)
                .build();
    }
    
    // Helper method to get current logged-in user
    private User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("DEBUG - Authentication object: " + authentication);
            
            if (authentication == null || !authentication.isAuthenticated() || 
                authentication.getPrincipal().equals("anonymousUser")) {
                System.out.println("DEBUG - No authentication found or anonymous user");
                return null;
            }
            
            String username = authentication.getName();
            System.out.println("DEBUG - Authenticated username: " + username);
            User user = userRepository.findByEmailOrUsername(username, username).orElse(null);
            System.out.println("DEBUG - Found user: " + (user != null ? user.getUsername() : "null"));
            return user;
        } catch (Exception e) {
            System.out.println("DEBUG - Exception in getCurrentUser: " + e.getMessage());
            return null;
        }
    }
}