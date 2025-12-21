package com._Blog.mojebbari.controllers;

import com._Blog.mojebbari.dto.CommentRequest;
import com._Blog.mojebbari.dto.CommentResponse;
import com._Blog.mojebbari.services.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * Add a comment to a post
     * POST /api/posts/{postId}/comments
     * 
     * Request Body:
     * {
     *   "content": "Great post!"
     * }
     * 
     * Response: CommentResponse (201 CREATED)
     */
    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequest request,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        CommentResponse response = commentService.addComment(postId, userEmail, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all comments for a post (newest first)
     * GET /api/posts/{postId}/comments
     * 
     * Example: GET /api/posts/1/comments
     * Response: List of CommentResponse objects
     */
    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long postId) {
        List<CommentResponse> comments = commentService.getCommentsByPost(postId);
        return ResponseEntity.ok(comments);
    }

    /**
     * Update a comment (only author can update)
     * PUT /api/posts/{postId}/comments/{commentId}
     * 
     * Request Body:
     * {
     *   "content": "Updated comment text"
     * }
     * 
     * Response: CommentResponse (200 OK)
     */
    @PutMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequest request,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        CommentResponse response = commentService.updateComment(commentId, userEmail, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a comment (author or admin can delete)
     * DELETE /api/posts/{postId}/comments/{commentId}
     * 
     * Example: DELETE /api/posts/1/comments/5
     * Response: 200 OK with message
     */
    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<String> deleteComment(
            @PathVariable Long commentId,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        commentService.deleteComment(commentId, userEmail);
        return ResponseEntity.ok("Comment deleted successfully");
    }

    /**
     * Get comments by user (for user profile page)
     * GET /api/users/{userId}/comments
     * 
     * Example: GET /api/users/3/comments
     * Response: List of CommentResponse objects
     */
    @GetMapping("/users/{userId}/comments")
    public ResponseEntity<List<CommentResponse>> getCommentsByUser(@PathVariable Long userId) {
        List<CommentResponse> comments = commentService.getCommentsByUser(userId);
        return ResponseEntity.ok(comments);
    }

    /**
     * Get comment count for a post
     * GET /api/posts/{postId}/comments/count
     * 
     * Example: GET /api/posts/1/comments/count
     * Response: 15
     */
    @GetMapping("/{postId}/comments/count")
    public ResponseEntity<Long> getCommentCount(@PathVariable Long postId) {
        Long count = commentService.getCommentCount(postId);
        return ResponseEntity.ok(count);
    }
}
