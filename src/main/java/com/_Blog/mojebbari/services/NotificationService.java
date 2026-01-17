package com._Blog.mojebbari.services;

import com._Blog.mojebbari.dto.NotificationResponse;
import com._Blog.mojebbari.models.*;
import com._Blog.mojebbari.repositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * NotificationService - Business logic for notifications
 * 
 * Handles:
 * - Creating notifications for various events
 * - Retrieving user notifications
 * - Marking notifications as read
 * - Counting unread notifications
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * Create a notification when someone follows you
     */
    @Transactional
    public void createFollowNotification(User follower, User following) {
        Notification notification = Notification.builder()
                .recipient(following)
                .actor(follower)
                .type(NotificationType.FOLLOW)
                .message(follower.getUsername() + " started following you")
                .build();
        
        notificationRepository.save(notification);
    }

    /**
     * Create a notification when someone likes your post
     */
    @Transactional
    public void createLikeNotification(User liker, Post post) {
        // Don't notify if user likes their own post
        if (liker.getId().equals(post.getUser().getId())) {
            return;
        }

        Notification notification = Notification.builder()
                .recipient(post.getUser())
                .actor(liker)
                .type(NotificationType.LIKE)
                .post(post)
                .message(liker.getUsername() + " liked your post: " + post.getTitle())
                .build();
        
        notificationRepository.save(notification);
    }

    /**
     * Create a notification when someone comments on your post
     */
    @Transactional
    public void createCommentNotification(User commenter, Post post, Comment comment) {
        // Don't notify if user comments on their own post
        if (commenter.getId().equals(post.getUser().getId())) {
            return;
        }

        Notification notification = Notification.builder()
                .recipient(post.getUser())
                .actor(commenter)
                .type(NotificationType.COMMENT)
                .post(post)
                .comment(comment)
                .message(commenter.getUsername() + " commented on your post: " + post.getTitle())
                .build();
        
        notificationRepository.save(notification);
    }

    /**
     * Create notifications for followers when user creates a new post
     */
    @Transactional
    public void createNewPostNotifications(User author, Post post, List<User> followers) {
        List<Notification> notifications = followers.stream()
                .map(follower -> Notification.builder()
                        .recipient(follower)
                        .actor(author)
                        .type(NotificationType.NEW_POST)
                        .post(post)
                        .message(author.getUsername() + " created a new post: " + post.getTitle())
                        .build())
                .collect(Collectors.toList());
        
        notificationRepository.saveAll(notifications);
    }

    /**
     * Get all notifications for current user
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(User currentUser) {
        List<Notification> notifications = notificationRepository
                .findByRecipientOrderByCreatedAtDesc(currentUser);
        
        return notifications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get only unread notifications for current user
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(User currentUser) {
        List<Notification> notifications = notificationRepository
                .findByRecipientAndIsReadOrderByCreatedAtDesc(currentUser, false);
        
        return notifications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Mark a single notification as read
     */
    @Transactional
    public void markAsRead(Long notificationId, User currentUser) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        
        // Ensure user can only mark their own notifications
        if (!notification.getRecipient().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("You can only mark your own notifications as read");
        }
        
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    /**
     * Mark all notifications as read for current user
     */
    @Transactional
    public void markAllAsRead(User currentUser) {
        List<Notification> unreadNotifications = notificationRepository
                .findByRecipientAndIsReadOrderByCreatedAtDesc(currentUser, false);
        
        unreadNotifications.forEach(notification -> notification.setIsRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }

    /**
     * Get count of unread notifications
     */
    @Transactional(readOnly = true)
    public Long getUnreadCount(User currentUser) {
        return notificationRepository.countByRecipientAndIsRead(currentUser, false);
    }

    /**
     * Delete all notifications for a user (when user is deleted)
     */
    @Transactional
    public void deleteAllForUser(User user) {
        notificationRepository.deleteByRecipient(user);
    }

    /**
     * Map Notification entity to NotificationResponse DTO
     */
    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .actorUsername(notification.getActor().getUsername())
                .actorProfilePicture(notification.getActor().getProfilePicture())
                .type(notification.getType())
                .message(notification.getMessage())
                .postId(notification.getPost() != null ? notification.getPost().getId() : null)
                .postTitle(notification.getPost() != null ? notification.getPost().getTitle() : null)
                .commentId(notification.getComment() != null ? notification.getComment().getId() : null)
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
