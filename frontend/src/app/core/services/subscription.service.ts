import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

/**
 * SubscriptionService - Handles all follow/subscription-related API calls
 * 
 * What it does:
 * 1. Follow a user
 * 2. Unfollow a user
 * 3. Get list of followers
 * 4. Get list of following
 * 5. Get follower count
 * 6. Get following count
 */

@Injectable({
  providedIn: 'root'
})
export class SubscriptionService {
  private apiUrl = `${environment.apiUrl}/subscriptions`;

  constructor(private http: HttpClient) { }

  /**
   * Follow a user
   * POST /api/subscriptions/follow/{username}
   */
  followUser(username: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/follow/${username}`, {});
  }

  /**
   * Unfollow a user
   * DELETE /api/subscriptions/unfollow/{username}
   */
  unfollowUser(username: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/unfollow/${username}`);
  }

  /**
   * Get list of users who follow a specific user
   * GET /api/subscriptions/followers/{username}
   */
  getFollowers(username: string): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/followers/${username}`);
  }

  /**
   * Get list of users that a specific user is following
   * GET /api/subscriptions/following/{username}
   */
  getFollowing(username: string): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/following/${username}`);
  }

  /**
   * Get follower count for a user
   * GET /api/subscriptions/followers/{username}/count
   */
  getFollowerCount(username: string): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/followers/${username}/count`);
  }

  /**
   * Get following count for a user
   * GET /api/subscriptions/following/{username}/count
   */
  getFollowingCount(username: string): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/following/${username}/count`);
  }
}
