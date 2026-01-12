import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  UserProfileResponse,
  UpdateProfileRequest
} from '../models/user.model';

/**
 * UserService - Handles all user profile-related API calls
 * 
 * What it does:
 * 1. Get user profile by username
 * 2. Update current user's profile
 */

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = `${environment.apiUrl}/users`;

  constructor(private http: HttpClient) { }

  /**
   * Get user profile by username
   * GET /api/users/{username}
   * Returns full profile with statistics (followers, following, posts count)
   */
  getUserProfile(username: string): Observable<UserProfileResponse> {
    return this.http.get<UserProfileResponse>(`${this.apiUrl}/${username}`);
  }

  /**
   * Update current user's profile
   * PUT /api/users/me
   */
  updateProfile(request: UpdateProfileRequest): Observable<UserProfileResponse> {
    return this.http.put<UserProfileResponse>(`${this.apiUrl}/me`, request);
  }
}
