package com._Blog.mojebbari.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Comment Response
 * 
 * This is what we send to the frontend when showing comments
 * 
 * Why not send Comment entity directly?
 * 1. Security: Don't expose database IDs unnecessarily
 * 2. Performance: Don't send full User/Post objects (circular reference issues)
 * 3. Flexibility: Can add computed fields
 * 4. API Contract: Clear structure for frontend
 * 
 * Example Response:
 * {
 *   "id": 1,
 *   "text": "Great post!",
 *   "authorId": 5,
 *   "authorUsername": "alice",
 *   "authorProfilePicture": "https://...",
 *   "createdAt": "2025-12-19T10:30:00",
 *   "updatedAt": "2025-12-19T10:30:00",
 *   "isEdited": false,
 *   "isOwnComment": true
 * }
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponse {
    
    /**
     * Comment ID
     * Needed for: edit, delete operations
     */
    private Long id;
    
    /**
     * The comment text content
     */
    private String text;
    
    /**
     * ID of the post this comment belongs to
     * Useful for navigation
     */
    private Long postId;
    
    /**
     * Author information
     * We include basic author info so frontend doesn't need extra API calls
     */
    private Long authorId;
    private String authorUsername;
    private String authorProfilePicture;
    
    /**
     * Timestamps
     */
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Was this comment edited?
     * 
     * Logic: isEdited = updatedAt > createdAt
     * 
     * UI can show:
     * - "Alice commented 2h ago"
     * - "Alice commented 2h ago (edited)"
     */
    private boolean isEdited;
    
    /**
     * Is this the current user's own comment?
     * 
     * Used for:
     * - Showing edit/delete buttons only on own comments
     * - Different styling for own vs others' comments
     */
    private boolean isOwnComment;
}
