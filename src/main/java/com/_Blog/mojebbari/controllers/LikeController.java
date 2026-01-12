package com._Blog.mojebbari.controllers;

import com._Blog.mojebbari.dto.LikeResponse;
import com._Blog.mojebbari.dto.MessageResponse;
import com._Blog.mojebbari.services.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    /**
     * Like a post
     * POST /api/posts/{postId}/likes
     * 
     * Example: POST /api/posts/1/likes
     * Response: 200 OK with JSON message
     */
    @PostMapping("/{postId}/likes")
    public ResponseEntity<MessageResponse> likePost(
            @PathVariable Long postId,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        likeService.likePost(postId, userEmail);
        return ResponseEntity.ok(new MessageResponse("Post liked successfully"));
    }

    /**
     * Unlike a post
     * DELETE /api/posts/{postId}/likes
     * 
     * Example: DELETE /api/posts/1/likes
     * Response: 200 OK with JSON message
     */
    @DeleteMapping("/{postId}/likes")
    public ResponseEntity<MessageResponse> unlikePost(
            @PathVariable Long postId,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        likeService.unlikePost(postId, userEmail);
        return ResponseEntity.ok(new MessageResponse("Post unliked successfully"));
    }

    /**
     * Get all users who liked this post
     * GET /api/posts/{postId}/likes
     * 
     * Example: GET /api/posts/1/likes
     * Response: List of LikeResponse objects
     */
    @GetMapping("/{postId}/likes")
    public ResponseEntity<List<LikeResponse>> getLikes(@PathVariable Long postId) {
        List<LikeResponse> likes = likeService.getLikesByPost(postId);
        return ResponseEntity.ok(likes);
    }

    /**
     * Check if current user liked this post
     * GET /api/posts/{postId}/likes/status
     * 
     * Example: GET /api/posts/1/likes/status
     * Response: {"liked": true}
     */
    @GetMapping("/{postId}/likes/status")
    public ResponseEntity<Boolean> isLiked(
            @PathVariable Long postId,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        boolean isLiked = likeService.isPostLikedByUser(postId, userEmail);
        return ResponseEntity.ok(isLiked);
    }

    /**
     * Get like count for a post
     * GET /api/posts/{postId}/likes/count
     * 
     * Example: GET /api/posts/1/likes/count
     * Response: 42
     */
    @GetMapping("/{postId}/likes/count")
    public ResponseEntity<Long> getLikeCount(@PathVariable Long postId) {
        Long count = likeService.getLikeCount(postId);
        return ResponseEntity.ok(count);
    }
}
