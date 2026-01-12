import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

/**
 * LikeService - Handles all like-related API calls
 * 
 * What it does:
 * 1. Like a post
 * 2. Unlike a post
 * 3. Get all users who liked a post
 * 4. Get like count for a post
 * 5. Check if current user liked a post
 */

@Injectable({
  providedIn: 'root'
})
export class LikeService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) { }

  /**
   * Like a post
   * POST /api/posts/{postId}/likes
   */
  likePost(postId: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/posts/${postId}/likes`, {});
  }

  /**
   * Unlike a post
   * DELETE /api/posts/{postId}/likes
   */
  unlikePost(postId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/posts/${postId}/likes`);
  }

  /**
   * Get all users who liked a post
   * GET /api/posts/{postId}/likes
   */
  getLikes(postId: number): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/posts/${postId}/likes`);
  }

  /**
   * Get like count for a post
   * GET /api/posts/{postId}/likes/count
   */
  getLikeCount(postId: number): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/posts/${postId}/likes/count`);
  }

  /**
   * Check if current user liked a post
   * GET /api/posts/{postId}/likes/isLiked
   */
  isLiked(postId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/posts/${postId}/likes/isLiked`);
  }
}
