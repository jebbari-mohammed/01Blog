package com._Blog.mojebbari.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating or updating a comment
 * 
 * This is what the frontend sends when:
 * - User posts a new comment
 * - User edits their comment
 * 
 * Example Request Body:
 * {
 *   "text": "Great post! Love the content!"
 * }
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentRequest {
    
    /**
     * The comment text content
     * 
     * Validation:
     * - @NotBlank: Cannot be empty or just whitespace
     * - @Size: Must be between 1 and 1000 characters
     * 
     * Why 1000 chars?
     * - Prevents spam (super long comments)
     * - Keeps comments readable
     * - Similar to Twitter's limits
     */
    @NotBlank(message = "Comment cannot be empty")
    @Size(min = 1, max = 1000, message = "Comment must be between 1 and 1000 characters")
    private String text;
}
