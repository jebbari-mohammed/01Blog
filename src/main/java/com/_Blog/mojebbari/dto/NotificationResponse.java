package com._Blog.mojebbari.dto;

import com._Blog.mojebbari.models.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * NotificationResponse - DTO for sending notification data to frontend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private String actorUsername;
    private String actorProfilePicture;
    private NotificationType type;
    private String message;
    private Long postId;        // If notification is about a post
    private String postTitle;   // Title of the post
    private Long commentId;     // If notification is about a comment
    private Boolean isRead;
    private LocalDateTime createdAt;
}
