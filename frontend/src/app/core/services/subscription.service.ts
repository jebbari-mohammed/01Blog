import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

/**
 * SubscriptionService - Handles all follow/subscription-related API calls
 * 
 * Backend API Endpoints:
 * - POST /api/subscriptions/{userId} - Follow a user
 * - DELETE /api/subscriptions/{userId} - Unfollow a user
 * - GET /api/subscriptions/following - Get my following list
 * - GET /api/subscriptions/followers - Get my followers list
 * - GET /api/subscriptions/{userId}/following - Get user's following list
 * - GET /api/subscriptions/{userId}/followers - Get user's followers list
 */

export interface UserSummary {
  id: number;
  username: string;
  email: string;
  bio?: string;
  profilePicture?: string;
  isFollowing: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class SubscriptionService {
  private apiUrl = `${environment.apiUrl}/subscriptions`;

  constructor(private http: HttpClient) { }

  /**
   * Follow a user by their user ID
   * POST /api/subscriptions/{userId}
   * @returns 201 Created with success message
   */
  followUser(userId: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/${userId}`, {});
  }

  /**
   * Unfollow a user by their user ID
   * DELETE /api/subscriptions/{userId}
   * @returns 204 No Content
   */
  unfollowUser(userId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${userId}`);
  }

  /**
   * Get list of users that the current logged-in user is following
   * GET /api/subscriptions/following
   * @returns Array of UserSummary with isFollowing flag
   */
  getMyFollowing(): Observable<UserSummary[]> {
    return this.http.get<UserSummary[]>(`${this.apiUrl}/following`);
  }

  /**
   * Get list of users who follow the current logged-in user
   * GET /api/subscriptions/followers
   * @returns Array of UserSummary with isFollowing flag
   */
  getMyFollowers(): Observable<UserSummary[]> {
    return this.http.get<UserSummary[]>(`${this.apiUrl}/followers`);
  }

  /**
   * Get list of users that a specific user is following
   * GET /api/subscriptions/{userId}/following
   * @param userId - The ID of the user to check
   * @returns Array of UserSummary
   */
  getUserFollowing(userId: number): Observable<UserSummary[]> {
    return this.http.get<UserSummary[]>(`${this.apiUrl}/${userId}/following`);
  }

  /**
   * Get list of users who follow a specific user
   * GET /api/subscriptions/{userId}/followers
   * @param userId - The ID of the user to check
   * @returns Array of UserSummary
   */
  getUserFollowers(userId: number): Observable<UserSummary[]> {
    return this.http.get<UserSummary[]>(`${this.apiUrl}/${userId}/followers`);
  }

  /**
   * Check if current user is following a specific user
   * Uses the isFollowing flag from the UserSummary response
   */
  isFollowing(userId: number): Observable<boolean> {
    // We can check this by getting the following list and finding the user
    // Or we could add a dedicated endpoint in the backend if needed
    return new Observable(observer => {
      this.getMyFollowing().subscribe({
        next: (following) => {
          const isFollowing = following.some(user => user.id === userId);
          observer.next(isFollowing);
          observer.complete();
        },
        error: (err) => observer.error(err)
      });
    });
  }
}
