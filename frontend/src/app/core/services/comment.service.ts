import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  Comment,
  CreateCommentRequest,
  UpdateCommentRequest
} from '../models/comment.model';

/**
 * CommentService - Handles all comment-related API calls
 * 
 * What it does:
 * 1. Add a comment to a post
 * 2. Get all comments for a post
 * 3. Update a comment
 * 4. Delete a comment
 * 5. Get comment count for a post
 */

@Injectable({
  providedIn: 'root'
})
export class CommentService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) { }

  /**
   * Add a comment to a post
   * POST /api/posts/{postId}/comments
   */
  addComment(postId: number, request: CreateCommentRequest): Observable<Comment> {
    return this.http.post<Comment>(`${this.apiUrl}/posts/${postId}/comments`, request);
  }

  /**
   * Get all comments for a post
   * GET /api/posts/{postId}/comments
   */
  getComments(postId: number): Observable<Comment[]> {
    return this.http.get<Comment[]>(`${this.apiUrl}/posts/${postId}/comments`);
  }

  /**
   * Update a comment
   * PUT /api/comments/{commentId}
   */
  updateComment(commentId: number, request: UpdateCommentRequest): Observable<Comment> {
    return this.http.put<Comment>(`${this.apiUrl}/comments/${commentId}`, request);
  }

  /**
   * Delete a comment
   * DELETE /api/comments/{commentId}
   */
  deleteComment(commentId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/comments/${commentId}`);
  }

  /**
   * Get comment count for a post
   * GET /api/posts/{postId}/comments/count
   */
  getCommentCount(postId: number): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/posts/${postId}/comments/count`);
  }
}
