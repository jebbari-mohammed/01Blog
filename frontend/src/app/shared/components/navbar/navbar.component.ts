import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { MaterialModule } from '../../material.module';
import { AuthService } from '../../../core/services/auth.service';
import { User } from '../../../core/models/user.model';

/**
 * NavbarComponent - Navigation bar at the top of the app
 * 
 * What it shows:
 * - Logo and app name
 * - Home link
 * - If logged in: Create Post button, Profile dropdown, Logout
 * - If not logged in: Login and Register buttons
 */

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule, MaterialModule],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.scss'
})
export class NavbarComponent implements OnInit {
  // Current logged-in user (null if not logged in)
  currentUser: User | null = null;

  constructor(
    public authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Subscribe to currentUser$ to know when user logs in/out
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
    });
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
