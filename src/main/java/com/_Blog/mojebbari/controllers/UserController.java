package com._Blog.mojebbari.controllers;

import com._Blog.mojebbari.dto.UpdateProfileRequest;
import com._Blog.mojebbari.dto.UserProfileResponse;
import com._Blog.mojebbari.models.User;
import com._Blog.mojebbari.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * Controller for user profile operations
 * 
 * Endpoints:
 * - GET /api/users/{username} - View any user's profile
 * - GET /api/users/me - View own profile
 * - PUT /api/users/me - Update own profile
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * GET /api/users/me
     * 
     * Get the currently logged-in user's profile
     * 
     * Example Response:
     * {
     *   "id": 1,
     *   "username": "alice",
     *   "bio": "Learning Spring Boot!",
     *   "followerCount": 10,
     *   "followingCount": 5,
     *   "postCount": 20,
     *   "isOwnProfile": true,
     *   "isFollowing": false
     * }
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUserProfile(Principal principal) {
        // Extract User from Principal
        User currentUser = (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        
        UserProfileResponse profile = userService.getUserProfile(
                currentUser.getUsername(), 
                currentUser.getId()
        );
        
        return ResponseEntity.ok(profile);
    }

    /**
     * GET /api/users/{username}
     * 
     * View any user's public profile
     * 
     * @param username - Username of the profile to view
     * @param principal - The logged-in user (optional, for isFollowing check)
     */
    @GetMapping("/{username}")
    public ResponseEntity<UserProfileResponse> getUserProfile(
            @PathVariable String username,
            Principal principal
    ) {
        Long currentUserId = null;
        
        // If user is logged in, get their ID
        if (principal != null) {
            User currentUser = (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
            currentUserId = currentUser.getId();
        }
        
        UserProfileResponse profile = userService.getUserProfile(username, currentUserId);
        return ResponseEntity.ok(profile);
    }

    /**
     * PUT /api/users/me
     * 
     * Update own profile
     * 
     * Request Body Example:
     * {
     *   "username": "alice_updated",
     *   "bio": "New bio here",
     *   "profilePicture": "https://example.com/pic.jpg",
     *   "coverImage": "https://example.com/cover.jpg"
     * }
     * 
     * All fields are optional - only send what you want to update
     */
    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Principal principal
    ) {
        // Get current user
        User currentUser = (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        
        // Update profile
        UserProfileResponse updatedProfile = userService.updateProfile(
                currentUser.getId(), 
                request
        );
        
        return ResponseEntity.ok(updatedProfile);
    }
}
