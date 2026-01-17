import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { MaterialModule } from '../../material.module';
import { AuthService } from '../../../core/services/auth.service';
import { ReportService } from '../../../core/services/report.service';
import { User } from '../../../core/models/user.model';

/**
 * NavbarComponent - Navigation bar at the top of the app
 * 
 * What it shows:
 * - Logo and app name
 * - Home link
 * - If logged in: Create Post button, Profile dropdown, Logout
 * - If admin: Admin Panel link with pending reports badge
 * - If not logged in: Login and Register buttons
 */

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule, MaterialModule],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.scss'
})
export class NavbarComponent implements OnInit, OnDestroy {
  // Current logged-in user (null if not logged in)
  currentUser: User | null = null;
  
  // Pending reports count (for admin badge)
  pendingReportsCount: number = 0;
  
  // Interval for polling pending reports
  private reportsInterval?: number;

  constructor(
    public authService: AuthService,
    private reportService: ReportService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Subscribe to currentUser$ to know when user logs in/out
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      
      // DEBUG: Log current user to console
      console.log('🔍 Navbar - Current User:', user);
      console.log('🔍 Navbar - User Role:', user?.role);
      console.log('🔍 Navbar - Is Admin?:', user?.role === 'ADMIN');
      
      // If user is admin, start polling for pending reports
      if (user?.role === 'ADMIN') {
        this.loadPendingReportsCount();
        this.startReportsPolling();
      } else {
        this.stopReportsPolling();
        this.pendingReportsCount = 0;
      }
    });
  }

  ngOnDestroy(): void {
    // Clean up interval when component is destroyed
    this.stopReportsPolling();
  }

  /**
   * Load pending reports count for admin badge
   */
  private loadPendingReportsCount(): void {
    this.reportService.getPendingReportsCount().subscribe({
      next: (response) => {
        this.pendingReportsCount = response.count;
      },
      error: (error) => {
        console.error('Error loading pending reports count:', error);
      }
    });
  }

  /**
   * Start polling for pending reports every 30 seconds
   */
  private startReportsPolling(): void {
    // Poll every 30 seconds
    this.reportsInterval = window.setInterval(() => {
      this.loadPendingReportsCount();
    }, 30000);
  }

  /**
   * Stop polling for pending reports
   */
  private stopReportsPolling(): void {
    if (this.reportsInterval) {
      clearInterval(this.reportsInterval);
      this.reportsInterval = undefined;
    }
  }

  /**
   * Logout the current user
   */
  logout(): void {
    this.authService.logout();
  }

  /**
   * Navigate to current user's profile
   */
  goToProfile(): void {
    if (this.currentUser && this.currentUser.username) {
      this.router.navigate(['/profile', this.currentUser.username]);
    } else {
      console.error('Cannot navigate to profile: username is not available');
      // You could show a snackbar message to the user here
    }
  }
}
