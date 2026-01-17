import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { MatSnackBar } from '@angular/material/snack-bar';

/**
 * Admin Guard - Protects admin-only routes
 * 
 * How it works:
 * 1. Checks if user is logged in
 * 2. Checks if user has ADMIN role
 * 3. If both true, allows access
 * 4. If false, redirects to home with error message
 * 
 * Usage in routes:
 * {
 *   path: 'admin',
 *   component: AdminDashboardComponent,
 *   canActivate: [adminGuard]  // <-- Add this
 * }
 */
export const adminGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const snackBar = inject(MatSnackBar);

  // Check if user is logged in
  if (!authService.isLoggedIn()) {
    snackBar.open('Please login to access the admin panel', 'Close', {
      duration: 3000
    });
    router.navigate(['/auth/login'], {
      queryParams: { returnUrl: state.url }
    });
    return false;
  }

  // Check if user is an admin
  const currentUser = authService.getCurrentUser();
  if (currentUser?.role !== 'ADMIN') {
    snackBar.open('Access denied. Admin privileges required.', 'Close', {
      duration: 3000
    });
    router.navigate(['/home']);
    return false;
  }

  // User is logged in AND is an admin
  return true;
};
