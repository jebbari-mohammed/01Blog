package com._Blog.mojebbari.repositories;

import com._Blog.mojebbari.models.Notification;
import com._Blog.mojebbari.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * NotificationRepository - Database operations for notifications
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Get all notifications for a user, ordered by newest first
     */
    List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient);

    /**
     * Get unread notifications for a user
     */
    List<Notification> findByRecipientAndIsReadOrderByCreatedAtDesc(User recipient, Boolean isRead);

    /**
     * Count unread notifications for a user
     */
    Long countByRecipientAndIsRead(User recipient, Boolean isRead);

    /**
     * Delete all notifications for a user
     */
    void deleteByRecipient(User recipient);
}
