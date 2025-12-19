package com._Blog.mojebbari.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating user profile
 * 
 * User can update their:
 * - Username (display name)
 * - Bio
 * - Profile picture URL
 * - Cover image URL
 * 
 * Things they CANNOT update here:
 * - Email (would require verification)
 * - Password (separate endpoint)
 * - Role (only admins can change roles)
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProfileRequest {
    
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;
    
    @Size(max = 500, message = "Bio cannot exceed 500 characters")
    private String bio;
    
    private String profilePicture; // URL to profile image
    
    private String coverImage; // URL to cover image
    
    // NOTE: All fields are optional (user doesn't have to update everything)
    // Service layer will only update non-null fields
}
