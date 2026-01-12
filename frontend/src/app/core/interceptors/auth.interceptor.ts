import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

/**
 * AuthInterceptor - Automatically adds JWT token to HTTP requests
 * 
 * How it works:
 * 1. Intercepts EVERY HTTP request before it's sent
 * 2. Gets the access token from AuthService
 * 3. If token exists, adds it to request headers
 * 4. Format: "Authorization: Bearer <token>"
 * 5. Backend receives request with token and validates it
 * 
 * Why this is useful:
 * - We don't have to manually add token to every HTTP request
 * - It's done automatically for all requests
 * - One place to manage authentication headers
 */

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // Inject AuthService to get the token
  const authService = inject(AuthService);
  
  // Skip adding token for auth endpoints (register/login don't need authentication)
  const isAuthEndpoint = req.url.includes('/api/auth/register') || 
                         req.url.includes('/api/auth/login');
  
  // Get the access token
  const token = authService.getAccessToken();

  // If token exists AND it's not an auth endpoint, add Authorization header
  if (token && !isAuthEndpoint && token.trim() !== '') {
    // We can't modify the original request, so we clone it
    req = req.clone({
      setHeaders: {
        // Add Authorization header with Bearer token
        // Format: "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        Authorization: `Bearer ${token}`
      }
    });
  }

  // Pass the request to the next handler
  return next(req);
};
