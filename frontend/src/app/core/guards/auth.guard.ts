import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * AuthGuard - Protects routes from unauthorized access
 * 
 * How it works:
 * 1. Check if user is logged in using AuthService
 * 2. If logged in -> allow access to the route
 * 3. If not logged in -> redirect to login page
 * 
 * Usage in routes:
 * {
 *   path: 'profile',
 *   component: ProfileComponent,
 *   canActivate: [authGuard]  // <-- This protects the route
 * }
 */

export const authGuard: CanActivateFn = (route, state) => {
  // Inject services (Angular 17+ way)
  const authService = inject(AuthService);
  const router = inject(Router);

  // Check if user is logged in
  if (authService.isLoggedIn()) {
    // User is logged in, allow access
    return true;
  }

  // User is not logged in, redirect to login page
  // We pass the returnUrl so after login, user goes back to where they wanted to go
  router.navigate(['/auth/login'], {
    queryParams: { returnUrl: state.url }
  });
  
  return false;
};
