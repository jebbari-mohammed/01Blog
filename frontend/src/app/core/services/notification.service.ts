import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, timer } from 'rxjs';
import { switchMap, shareReplay } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { Notification } from '../models/notification.model';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private apiUrl = `${environment.apiUrl}/notifications`;
  
  // Poll for unread count every 30 seconds
  unreadCount$ = timer(0, 30000).pipe(
    switchMap(() => this.getUnreadCount()),
    shareReplay(1)
  );

  constructor(private http: HttpClient) {}

  /**
   * Get all notifications for current user
   */
  getMyNotifications(): Observable<Notification[]> {
    return this.http.get<Notification[]>(this.apiUrl);
  }

  /**
   * Get only unread notifications
   */
  getUnreadNotifications(): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.apiUrl}/unread`);
  }

  /**
   * Get count of unread notifications (for badge)
   */
  getUnreadCount(): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/unread-count`);
  }

  /**
   * Mark a specific notification as read
   */
  markAsRead(notificationId: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${notificationId}/read`, {});
  }

  /**
   * Mark all notifications as read
   */
  markAllAsRead(): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/read-all`, {});
  }
}
