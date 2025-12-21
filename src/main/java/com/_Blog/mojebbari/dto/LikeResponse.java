package com._Blog.mojebbari.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Like Response
 * 
 * Used when showing list of users who liked a post
 * 
 * Example: "Liked by Alice, Bob, and 10 others"
 * 
 * Example Response:
 * {
 *   "userId": 5,
 *   "username": "alice",
 *   "profilePicture": "https://...",
 *   "likedAt": "2025-12-19T10:30:00"
 * }
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LikeResponse {
    
    /**
     * ID of the user who liked
     */
    private Long userId;
    
    /**
     * Username of the user
     */
    private String username;
    
    /**
     * Profile picture URL
     * For displaying avatars in "Liked by..." list
     */
    private String profilePicture;
    
    /**
     * When they liked the post
     * Can show "Alice liked this 2 hours ago"
     */
    private LocalDateTime likedAt;
}
