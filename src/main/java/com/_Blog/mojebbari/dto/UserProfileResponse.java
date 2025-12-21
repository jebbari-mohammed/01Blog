package com._Blog.mojebbari.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


/**
 * DTO for User Profile Response
 * 
 * This is what we send to the frontend when someone views a profile
 * 
 * Why use a DTO instead of sending the User entity directly?
 * 1. Security: Don't expose password, email (if private), etc.
 * 2. Performance: Only send what's needed
 * 3. Flexibility: Can add computed fields like followerCount
 * 4. API Contract: Clear structure for frontend developers
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponse {
    
    private Long id;
    private String username;
    private String email; // Consider making this private/optional
    private String bio;
    private String profilePicture;
    private String coverImage;
    private LocalDateTime createdAt;
    
    // Computed fields (not in User entity)
    private long followerCount;    // How many followers this user has
    private long followingCount;   // How many users this user follows
    private int postCount;          // How many posts this user has
    
    /**
     * Whether the current logged-in user is following this profile
     * Example: If Alice views Bob's profile, this shows if Alice follows Bob
     */
    private boolean isFollowing;
    
    /**
     * Whether this is the current user's own profile
     * Example: If Alice views Alice's profile, this is true
     * Used to show "Edit Profile" button
     */
    private boolean isOwnProfile;
}
