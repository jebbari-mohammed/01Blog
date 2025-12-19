package com._Blog.mojebbari.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for a simple user summary in lists
 * 
 * Used when showing lists of followers/following
 * Lighter than UserProfileResponse (doesn't include counts, etc.)
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserSummaryResponse {
    
    private Long id;
    private String username;
    private String bio;
    private String profilePicture;
    
    /**
     * Whether the current logged-in user is following this user
     * Useful for showing "Follow/Unfollow" button in lists
     */
    private boolean isFollowing;
    
    /**
     * When this user joined (or when they followed, depending on context)
     */
    private LocalDateTime createdAt;
}
