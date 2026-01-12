import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  Post,
  CreatePostRequest,
  UpdatePostRequest,
  PaginatedPostsResponse
} from '../models/post.model';

/**
 * PostService - Handles all post-related API calls
 * 
 * What it does:
 * 1. Create new posts
 * 2. Get all posts (with pagination)
 * 3. Get posts by username
 * 4. Get a single post by ID
 * 5. Update posts
 * 6. Delete posts
 */

@Injectable({
  providedIn: 'root'
})
export class PostService {
  // API endpoint for posts
  private apiUrl = `${environment.apiUrl}/posts`;

  constructor(private http: HttpClient) { }

  /**
   * Create a new post
   * POST /api/posts
   */
  createPost(request: CreatePostRequest): Observable<Post> {
    return this.http.post<Post>(this.apiUrl, request);
  }

  /**
   * Get all posts with pagination
   * GET /api/posts?page=0&size=10&sortBy=createdAt
   * 
   * Parameters:
   * - page: which page to get (0 is first page)
   * - size: how many posts per page
   * - sortBy: sort by field (createdAt, title, etc.)
   */
  getAllPosts(page: number = 0, size: number = 10, sortBy: string = 'createdAt'): Observable<PaginatedPostsResponse> {
    // HttpParams helps us build query parameters (?page=0&size=10)
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy);

    return this.http.get<PaginatedPostsResponse>(this.apiUrl, { params });
  }

  /**
   * Get posts by a specific user
   * GET /api/posts/user/{username}
   */
  getUserPosts(username: string): Observable<Post[]> {
    return this.http.get<Post[]>(`${this.apiUrl}/user/${username}`);
  }

  /**
   * Get posts by a specific user (paginated) - if backend supports it
   * GET /api/posts/user/{username}?page=0&size=10
   */
  getPostsByUsername(username: string, page: number = 0, size: number = 10): Observable<PaginatedPostsResponse> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PaginatedPostsResponse>(`${this.apiUrl}/user/${username}`, { params });
  }

  /**
   * Get a single post by ID
   * GET /api/posts/{id}
   */
  getPostById(id: number): Observable<Post> {
    return this.http.get<Post>(`${this.apiUrl}/${id}`);
  }

  /**
   * Update an existing post
   * PUT /api/posts/{id}
   */
  updatePost(id: number, request: UpdatePostRequest): Observable<Post> {
    return this.http.put<Post>(`${this.apiUrl}/${id}`, request);
  }

  /**
   * Delete a post
   * DELETE /api/posts/{id}
   */
  deletePost(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
