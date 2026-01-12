import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { Router } from '@angular/router';
import { environment } from '../../../environments/environment';
import {
  LoginRequest,
  RegisterRequest,
  AuthenticationResponse,
  RefreshTokenRequest
} from '../models/auth.model';
import { User } from '../models/user.model';
import { jwtDecode } from 'jwt-decode';

/**
 * AuthService - Handles all authentication logic
 * 
 * What it does:
 * 1. Login/Register users
 * 2. Store JWT tokens in localStorage
 * 3. Decode tokens to get user info
 * 4. Check if user is logged in
 * 5. Logout users
 * 6. Refresh expired tokens
 */

@Injectable({
  providedIn: 'root'  // This service is available app-wide
})
export class AuthService {
  // API endpoint for authentication
  private apiUrl = `${environment.apiUrl}/auth`;

  // BehaviorSubject - holds the current user and notifies subscribers when it changes
  // null means no user is logged in
  private currentUserSubject: BehaviorSubject<User | null>;
  
  // Observable that components can subscribe to for user changes
  public currentUser$: Observable<User | null>;

  constructor(
    private http: HttpClient,  // For making HTTP requests
    private router: Router     // For navigation
  ) {
    // On initialization, check if user is already logged in
    // by decoding the stored token
    const storedUser = this.getUserFromToken();
    this.currentUserSubject = new BehaviorSubject<User | null>(storedUser);
    this.currentUser$ = this.currentUserSubject.asObservable();
  }

  /**
   * Register a new user
   * Steps:
   * 1. Send username, email, password to backend
   * 2. Backend creates user in database
   * 3. Backend returns JWT tokens
   * 4. We store tokens and decode user info
   */
  register(request: RegisterRequest): Observable<AuthenticationResponse> {
    return this.http.post<AuthenticationResponse>(`${this.apiUrl}/register`, request)
      .pipe(
        tap(response => {
          // tap() allows us to perform side effects without changing the response
          this.handleAuthenticationResponse(response);
        })
      );
  }

  /**
   * Login an existing user
   * Steps:
   * 1. Send email and password to backend
   * 2. Backend validates credentials
   * 3. Backend returns JWT tokens if valid
   * 4. We store tokens and decode user info
   */
  login(request: LoginRequest): Observable<AuthenticationResponse> {
    return this.http.post<AuthenticationResponse>(`${this.apiUrl}/login`, request)
      .pipe(
        tap(response => {
          this.handleAuthenticationResponse(response);
        })
      );
  }

  /**
   * Logout the current user
   * Steps:
   * 1. Remove tokens from localStorage
   * 2. Set currentUser to null
   * 3. Navigate to login page
   */
  logout(): void {
    // Remove tokens from browser storage
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    
    // Update current user to null (no one logged in)
    this.currentUserSubject.next(null);
    
    // Redirect to login page
    this.router.navigate(['/auth/login']);
  }

  /**
   * Refresh the access token when it expires
   * Access tokens expire after 10 hours
   * Refresh tokens expire after 7 days
   */
  refreshToken(): Observable<AuthenticationResponse> {
    const refreshToken = this.getRefreshToken();
    
    if (!refreshToken) {
      throw new Error('No refresh token available');
    }

    const request: RefreshTokenRequest = { refreshToken };
    
    return this.http.post<AuthenticationResponse>(`${this.apiUrl}/refresh`, request)
      .pipe(
        tap(response => {
          // Store the new access token (backend field is 'token' not 'accessToken')
          this.setAccessToken(response.token);
        })
      );
  }

  /**
   * Check if user is currently logged in
   * Returns true if we have a valid access token
   */
  isLoggedIn(): boolean {
    const token = this.getAccessToken();
    if (!token) {
      return false;
    }

    // Check if token is expired
    return !this.isTokenExpired(token);
  }

  /**
   * Get the current user object
   * Returns null if no one is logged in
   */
  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  /**
   * Get the access token from localStorage
   */
  getAccessToken(): string | null {
    return localStorage.getItem('token');
  }

  /**
   * Get the refresh token from localStorage
   */
  getRefreshToken(): string | null {
    return localStorage.getItem('refreshToken');
  }

  /**
   * Private helper methods
   */

  // Handle successful authentication (login or register)
  private handleAuthenticationResponse(response: AuthenticationResponse): void {
    // Store tokens in localStorage (backend field is 'token' not 'accessToken')
    this.setAccessToken(response.token);
    this.setRefreshToken(response.refreshToken);

    // Decode token to get user information
    const user = this.getUserFromToken();
    
    // Update current user (this notifies all subscribers)
    this.currentUserSubject.next(user);
  }

  // Store access token in localStorage
  private setAccessToken(token: string): void {
    localStorage.setItem('token', token);
  }

  // Store refresh token in localStorage
  private setRefreshToken(token: string): void {
    localStorage.setItem('refreshToken', token);
  }

  // Decode JWT token to extract user information
  private getUserFromToken(): User | null {
    const token = this.getAccessToken();
    
    if (!token) {
      return null;
    }

    try {
      // JWT tokens have 3 parts: header.payload.signature
      // The payload contains the user information
      const decoded: any = jwtDecode(token);
      
      // Extract user info from token payload
      return {
        id: decoded.userId,
        username: decoded.username,
        email: decoded.sub,  // 'sub' is the standard JWT field for subject (email in our case)
        role: decoded.role,
        createdAt: decoded.createdAt
      };
    } catch (error) {
      console.error('Error decoding token:', error);
      return null;
    }
  }

  // Check if token is expired
  private isTokenExpired(token: string): boolean {
    try {
      const decoded: any = jwtDecode(token);
      
      // 'exp' is the standard JWT field for expiration time (in seconds)
      const expirationDate = decoded.exp * 1000;  // Convert to milliseconds
      const now = Date.now();
      
      return expirationDate < now;
    } catch (error) {
      return true;  // If we can't decode, consider it expired
    }
  }
}
