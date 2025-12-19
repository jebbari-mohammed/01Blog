package com._Blog.mojebbari.controllers;

import com._Blog.mojebbari.dto.UserSummaryResponse;
import com._Blog.mojebbari.models.User;
import com._Blog.mojebbari.services.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * Controller for subscription operations (follow/unfollow)
 * 
 * Endpoints:
 * - POST /api/subscriptions/{userId} - Follow a user
 * - DELETE /api/subscriptions/{userId} - Unfollow a user
 * - GET /api/subscriptions/following - Get users I follow
 * - GET /api/subscriptions/followers - Get my followers
 * - GET /api/subscriptions/{userId}/following - Get users this user follows
 * - GET /api/subscriptions/{userId}/followers - Get this user's followers
 */
@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    /**
     * POST /api/subscriptions/{userId}
     * 
     * Follow a user
     * 
     * Example: POST /api/subscriptions/5
     * This makes the current user follow user with ID 5
     * 
     * Returns: 201 Created
     */
    @PostMapping("/{userId}")
    public ResponseEntity<Void> followUser(
            @PathVariable Long userId,
            Principal principal
    ) {
        User currentUser = (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        
        subscriptionService.followUser(currentUser.getId(), userId);
        
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * DELETE /api/subscriptions/{userId}
     * 
     * Unfollow a user
     * 
     * Example: DELETE /api/subscriptions/5
     * This makes the current user unfollow user with ID 5
     * 
     * Returns: 204 No Content
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> unfollowUser(
            @PathVariable Long userId,
            Principal principal
    ) {
        User currentUser = (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        
        subscriptionService.unfollowUser(currentUser.getId(), userId);
        
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/subscriptions/following
     * 
     * Get list of users that I am following
     * 
     * Response Example:
     * [
     *   {
     *     "id": 2,
     *     "username": "bob",
     *     "bio": "Learning Java",
     *     "profilePicture": "...",
     *     "isFollowing": true,
     *     "createdAt": "2025-01-15T10:30:00"
     *   },
     *   ...
     * ]
     */
    @GetMapping("/following")
    public ResponseEntity<List<UserSummaryResponse>> getMyFollowing(Principal principal) {
        User currentUser = (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        
        List<UserSummaryResponse> following = subscriptionService.getFollowing(
                currentUser.getId(), 
                currentUser.getId()
        );
        
        return ResponseEntity.ok(following);
    }

    /**
     * GET /api/subscriptions/followers
     * 
     * Get list of my followers
     * 
     * Response: List of UserSummaryResponse (same structure as above)
     */
    @GetMapping("/followers")
    public ResponseEntity<List<UserSummaryResponse>> getMyFollowers(Principal principal) {
        User currentUser = (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        
        List<UserSummaryResponse> followers = subscriptionService.getFollowers(
                currentUser.getId(), 
                currentUser.getId()
        );
        
        return ResponseEntity.ok(followers);
    }

    /**
     * GET /api/subscriptions/{userId}/following
     * 
     * Get list of users that a specific user is following
     * 
     * Example: GET /api/subscriptions/5/following
     * Returns list of users that user #5 follows
     * 
     * @param userId - The user whose following list we want to see
     */
    @GetMapping("/{userId}/following")
    public ResponseEntity<List<UserSummaryResponse>> getUserFollowing(
            @PathVariable Long userId,
            Principal principal
    ) {
        Long currentUserId = null;
        if (principal != null) {
            User currentUser = (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
            currentUserId = currentUser.getId();
        }
        
        List<UserSummaryResponse> following = subscriptionService.getFollowing(userId, currentUserId);
        return ResponseEntity.ok(following);
    }

    /**
     * GET /api/subscriptions/{userId}/followers
     * 
     * Get list of followers of a specific user
     * 
     * Example: GET /api/subscriptions/5/followers
     * Returns list of users who follow user #5
     * 
     * @param userId - The user whose followers we want to see
     */
    @GetMapping("/{userId}/followers")
    public ResponseEntity<List<UserSummaryResponse>> getUserFollowers(
            @PathVariable Long userId,
            Principal principal
    ) {
        Long currentUserId = null;
        if (principal != null) {
            User currentUser = (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
            currentUserId = currentUser.getId();
        }
        
        List<UserSummaryResponse> followers = subscriptionService.getFollowers(userId, currentUserId);
        return ResponseEntity.ok(followers);
    }
}
