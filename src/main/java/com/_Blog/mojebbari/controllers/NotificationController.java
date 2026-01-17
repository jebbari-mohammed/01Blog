package com._Blog.mojebbari.controllers;

import com._Blog.mojebbari.dto.NotificationResponse;
import com._Blog.mojebbari.models.User;
import com._Blog.mojebbari.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * NotificationController - REST API endpoints for notifications
 * 
 * All endpoints require authentication
 * 
 * Endpoints:
 * - GET /api/notifications - Get all notifications
 * - GET /api/notifications/unread - Get unread notifications only
 * - GET /api/notifications/unread-count - Get count of unread notifications
 * - PUT /api/notifications/{id}/read - Mark specific notification as read
 * - PUT /api/notifications/read-all - Mark all notifications as read
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Get all notifications for current user
     * 
     * @return List of notifications (read and unread)
     */
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(
            @AuthenticationPrincipal User currentUser) {
        List<NotificationResponse> notifications = notificationService.getMyNotifications(currentUser);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get only unread notifications for current user
     * 
     * @return List of unread notifications
     */
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications(
            @AuthenticationPrincipal User currentUser) {
        List<NotificationResponse> notifications = notificationService.getUnreadNotifications(currentUser);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get count of unread notifications (for badge)
     * 
     * @return Count of unread notifications
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(
            @AuthenticationPrincipal User currentUser) {
        Long count = notificationService.getUnreadCount(currentUser);
        return ResponseEntity.ok(count);
    }

    /**
     * Mark a specific notification as read
     * 
     * @param id Notification ID
     * @return 200 OK
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        notificationService.markAsRead(id, currentUser);
        return ResponseEntity.ok().build();
    }

    /**
     * Mark all notifications as read for current user
     * 
     * @return 200 OK
     */
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @AuthenticationPrincipal User currentUser) {
        notificationService.markAllAsRead(currentUser);
        return ResponseEntity.ok().build();
    }
}
