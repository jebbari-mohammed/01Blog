import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MaterialModule } from '../../material.module';
import { Router } from '@angular/router';
import { SubscriptionService, UserSummary } from '../../../core/services/subscription.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { environment } from '../../../../environments/environment';

/**
 * UserListDialogComponent - Reusable dialog to show followers/following lists
 * 
 * Features:
 * - Display list of users with avatars
 * - Follow/Unfollow buttons for each user
 * - Click on user to navigate to their profile
 * - Shows empty state if no users
 */

export interface UserListDialogData {
  title: string;
  users: UserSummary[];
  currentUserId: number;
}

@Component({
  selector: 'app-user-list-dialog',
  standalone: true,
  imports: [CommonModule, MaterialModule],
  templateUrl: './user-list-dialog.component.html',
  styleUrls: ['./user-list-dialog.component.scss']
})
export class UserListDialogComponent {
  users: UserSummary[];
  isLoading: { [userId: number]: boolean } = {};

  constructor(
    public dialogRef: MatDialogRef<UserListDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: UserListDialogData,
    private subscriptionService: SubscriptionService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.users = data.users;
  }

  /**
   * Navigate to user's profile and close dialog
   */
  viewProfile(username: string): void {
    this.dialogRef.close();
    this.router.navigate(['/profile', username]);
  }

  /**
   * Toggle follow/unfollow for a user
   */
  toggleFollow(user: UserSummary): void {
    if (this.isLoading[user.id]) return;

    this.isLoading[user.id] = true;

    if (user.isFollowing) {
      // Unfollow
      this.subscriptionService.unfollowUser(user.id).subscribe({
        next: () => {
          user.isFollowing = false;
          this.isLoading[user.id] = false;
          this.snackBar.open(`Unfollowed ${user.username}`, 'Close', { duration: 2000 });
        },
        error: (error) => {
          this.isLoading[user.id] = false;
          console.error('Error unfollowing user:', error);
          this.snackBar.open('Failed to unfollow user', 'Close', { duration: 3000 });
        }
      });
    } else {
      // Follow
      this.subscriptionService.followUser(user.id).subscribe({
        next: () => {
          user.isFollowing = true;
          this.isLoading[user.id] = false;
          this.snackBar.open(`Following ${user.username}`, 'Close', { duration: 2000 });
        },
        error: (error) => {
          this.isLoading[user.id] = false;
          console.error('Error following user:', error);
          this.snackBar.open('Failed to follow user', 'Close', { duration: 3000 });
        }
      });
    }
  }

  /**
   * Get initials from username for avatar
   */
  getInitials(username: string): string {
    return username.substring(0, 2).toUpperCase();
  }

  /**
   * Get full image URL
   */
  getFullImageUrl(url: string): string {
    if (!url) return '';
    if (url.startsWith('http')) return url;
    return `${environment.apiUrl.replace('/api', '')}${url}`;
  }

  /**
   * Check if this is the current user
   */
  isCurrentUser(userId: number): boolean {
    return userId === this.data.currentUserId;
  }

  /**
   * Close dialog
   */
  close(): void {
    this.dialogRef.close();
  }
}
