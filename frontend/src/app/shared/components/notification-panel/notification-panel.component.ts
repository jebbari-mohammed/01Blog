import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MaterialModule } from '../../material.module';
import { NotificationService } from '../../../core/services/notification.service';
import { Notification, NotificationType } from '../../../core/models/notification.model';

@Component({
  selector: 'app-notification-panel',
  standalone: true,
  imports: [CommonModule, RouterModule, MaterialModule],
  templateUrl: './notification-panel.component.html',
  styleUrl: './notification-panel.component.scss'
})
export class NotificationPanelComponent implements OnInit {
  notifications: Notification[] = [];
  isLoading = false;
  
  @Output() notificationClicked = new EventEmitter<void>();
  
  NotificationType = NotificationType;

  constructor(private notificationService: NotificationService) {}

  ngOnInit(): void {
    this.loadNotifications();
  }

  loadNotifications(): void {
    this.isLoading = true;
    this.notificationService.getMyNotifications().subscribe({
      next: (notifications) => {
        this.notifications = notifications;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading notifications:', error);
        this.isLoading = false;
      }
    });
  }

  markAsRead(notification: Notification): void {
    if (!notification.isRead) {
      this.notificationService.markAsRead(notification.id).subscribe({
        next: () => {
          notification.isRead = true;
        },
        error: (error) => console.error('Error marking notification as read:', error)
      });
    }
    // Emit event to close the menu
    this.notificationClicked.emit();
  }

  markAllAsRead(): void {
    this.notificationService.markAllAsRead().subscribe({
      next: () => {
        this.notifications.forEach(n => n.isRead = true);
      },
      error: (error) => console.error('Error marking all as read:', error)
    });
  }

  hasUnreadNotifications(): boolean {
    return this.notifications.some(n => !n.isRead);
  }

  getNotificationIcon(type: NotificationType): string {
    switch (type) {
      case NotificationType.FOLLOW:
        return 'person_add';
      case NotificationType.LIKE:
        return 'favorite';
      case NotificationType.COMMENT:
        return 'comment';
      case NotificationType.NEW_POST:
        return 'article';
      case NotificationType.REPORT_REVIEWED:
        return 'verified_user';
      default:
        return 'notifications';
    }
  }

  getNotificationLink(notification: Notification): string {
    // Link to the specific post if available
    if (notification.postId) {
      return `/posts/${notification.postId}`;
    }
    // Link to user profile for follow notifications
    if (notification.type === NotificationType.FOLLOW) {
      return `/profile/${notification.actorUsername}`;
    }
    // Default to home
    return '/home';
  }

  formatDate(date: Date | string): string {
    const d = new Date(date);
    const now = new Date();
    const diffMs = now.getTime() - d.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins}m ago`;
    if (diffHours < 24) return `${diffHours}h ago`;
    if (diffDays < 7) return `${diffDays}d ago`;
    
    return d.toLocaleDateString();
  }
}
