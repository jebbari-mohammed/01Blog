import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { MaterialModule } from '../../../shared/material.module';
import { MatDialog } from '@angular/material/dialog';
import { UserService } from '../../../core/services/user.service';
import { PostService } from '../../../core/services/post.service';
import { AuthService } from '../../../core/services/auth.service';
import { SubscriptionService } from '../../../core/services/subscription.service';
import { ReportService, ReportType } from '../../../core/services/report.service';
import { UserProfileResponse } from '../../../core/models/user.model';
import { Post } from '../../../core/models/post.model';
import { PostCardComponent } from '../../post/post-card/post-card.component';
import { UserListDialogComponent } from '../../../shared/components/user-list-dialog/user-list-dialog.component';
import { ReportDialogComponent } from '../../../shared/components/report-dialog/report-dialog.component';
import { MatSnackBar } from '@angular/material/snack-bar';
import { environment } from '../../../../environments/environment';

/**
 * ProfileComponent - Display user profile with their posts
 * 
 * Features:
 * - User info (avatar, bio, stats)
 * - User's posts feed
 * - Edit profile button (if own profile)
 * - Follow/Unfollow button (if not own profile) ✅
 */
@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MaterialModule,
    PostCardComponent
  ],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss']
})
export class ProfileComponent implements OnInit {
  profile: UserProfileResponse | null = null;
  posts: Post[] = [];
  isLoading = true;
  isLoadingPosts = true;
  username: string = '';
  isOwnProfile = false;
  isFollowing = false;
  isFollowLoading = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private userService: UserService,
    private postService: PostService,
    public authService: AuthService,
    private subscriptionService: SubscriptionService,
    private reportService: ReportService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    // Get username from route params
    this.route.params.subscribe(params => {
      this.username = params['username'];
      this.loadProfile();
      this.loadUserPosts();
    });
  }

  /**
   * Load user profile
   */
  private loadProfile(): void {
    this.isLoading = true;
    this.userService.getUserProfile(this.username).subscribe({
      next: (profile) => {
        // Convert relative image URLs to absolute URLs
        if (profile.profilePicture) {
          profile.profilePicture = this.getFullImageUrl(profile.profilePicture);
        }
        if (profile.coverImage) {
          profile.coverImage = this.getFullImageUrl(profile.coverImage);
        }
        
        this.profile = profile;
        this.isLoading = false;
        
        // Check if this is the current user's profile
        const currentUser = this.authService.getCurrentUser();
        this.isOwnProfile = currentUser?.username === this.username;
        
        // Check follow status if not own profile
        if (!this.isOwnProfile && profile.id) {
          this.checkFollowStatus(profile.id);
        }
      },
      error: (error) => {
        this.isLoading = false;
        console.error('Error loading profile:', error);
        
        if (error.status === 404) {
          this.snackBar.open('User not found', 'Close', {
            duration: 3000,
            panelClass: ['error-snackbar']
          });
          this.router.navigate(['/home']);
        } else {
          this.snackBar.open('Failed to load profile', 'Close', {
            duration: 3000,
            panelClass: ['error-snackbar']
          });
        }
      }
    });
  }

  /**
   * Load user's posts
   */
  private loadUserPosts(): void {
    this.isLoadingPosts = true;
    this.postService.getUserPosts(this.username).subscribe({
      next: (posts) => {
        this.posts = posts;
        this.isLoadingPosts = false;
      },
      error: (error) => {
        this.isLoadingPosts = false;
        console.error('Error loading posts:', error);
      }
    });
  }

  /**
   * Handle post deletion
   */
  onPostDeleted(postId: number): void {
    this.posts = this.posts.filter(post => post.id !== postId);
    
    // Update post count
    if (this.profile) {
      this.profile.postsCount--;
    }
  }

  /**
   * Check if current user is following this profile user
   */
  private checkFollowStatus(userId: number): void {
    this.subscriptionService.isFollowing(userId).subscribe({
      next: (isFollowing) => {
        this.isFollowing = isFollowing;
      },
      error: (error) => {
        console.error('Error checking follow status:', error);
      }
    });
  }

  /**
   * Toggle follow/unfollow for this user
   */
  toggleFollow(): void {
    if (!this.profile || this.isFollowLoading) return;

    this.isFollowLoading = true;

    if (this.isFollowing) {
      // Unfollow
      this.subscriptionService.unfollowUser(this.profile.id).subscribe({
        next: () => {
          this.isFollowing = false;
          this.isFollowLoading = false;
          
          // Update follower count
          if (this.profile) {
            this.profile.followersCount--;
          }
          
          this.snackBar.open(`Unfollowed ${this.profile?.username}`, 'Close', {
            duration: 3000
          });
        },
        error: (error) => {
          this.isFollowLoading = false;
          console.error('Error unfollowing user:', error);
          this.snackBar.open('Failed to unfollow user', 'Close', {
            duration: 3000,
            panelClass: ['error-snackbar']
          });
        }
      });
    } else {
      // Follow
      this.subscriptionService.followUser(this.profile.id).subscribe({
        next: () => {
          this.isFollowing = true;
          this.isFollowLoading = false;
          
          // Update follower count
          if (this.profile) {
            this.profile.followersCount++;
          }
          
          this.snackBar.open(`Following ${this.profile?.username}`, 'Close', {
            duration: 3000
          });
        },
        error: (error) => {
          this.isFollowLoading = false;
          console.error('Error following user:', error);
          
          if (error.status === 400) {
            this.snackBar.open(error.error.message || 'Cannot follow this user', 'Close', {
              duration: 3000,
              panelClass: ['error-snackbar']
            });
          } else {
            this.snackBar.open('Failed to follow user', 'Close', {
              duration: 3000,
              panelClass: ['error-snackbar']
            });
          }
        }
      });
    }
  }

  /**
   * Navigate to edit profile
   */
  editProfile(): void {
    this.router.navigate(['/profile/edit']);
  }

  /**
   * Open followers list dialog
   */
  showFollowers(): void {
    if (!this.profile) return;

    this.subscriptionService.getUserFollowers(this.profile.id).subscribe({
      next: (followers) => {
        const currentUser = this.authService.getCurrentUser();
        this.dialog.open(UserListDialogComponent, {
          width: '500px',
          maxWidth: '90vw',
          data: {
            title: 'Followers',
            users: followers,
            currentUserId: currentUser?.id || 0
          }
        });
      },
      error: (error) => {
        console.error('Error loading followers:', error);
        this.snackBar.open('Failed to load followers', 'Close', {
          duration: 3000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  /**
   * Show following dialog
   */
  showFollowing(): void {
    if (!this.profile) return;

    this.subscriptionService.getUserFollowing(this.profile.id).subscribe({
      next: (following) => {
        const currentUser = this.authService.getCurrentUser();
        this.dialog.open(UserListDialogComponent, {
          width: '500px',
          maxWidth: '90vw',
          data: {
            title: 'Following',
            users: following,
            currentUserId: currentUser?.id || 0
          }
        });
      },
      error: (error) => {
        console.error('Error loading following:', error);
        this.snackBar.open('Failed to load following', 'Close', {
          duration: 3000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  /**
   * Report this user
   */
  reportUser(): void {
    if (!this.profile) return;

    const dialogRef = this.dialog.open(ReportDialogComponent, {
      width: '500px',
      maxWidth: '90vw',
      data: {
        contentType: ReportType.USER,
        contentId: this.profile.id,
        contentPreview: `@${this.profile.username}${this.profile.bio ? ' - ' + this.profile.bio : ''}`
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        // Report submitted successfully (handled in dialog)
      }
    });
  }

  /**
   * Get initials from username for avatar
   */
  getInitials(username: string): string {
    return username.substring(0, 2).toUpperCase();
  }

  /**
   * Format join date
   */
  formatJoinDate(date: string): string {
    return new Date(date).toLocaleDateString('en-US', { 
      year: 'numeric', 
      month: 'long'
    });
  }

  /**
   * Get full image URL
   */
  private getFullImageUrl(url: string): string {
    if (url.startsWith('http')) return url;
    return `${environment.apiUrl.replace('/api', '')}${url}`;
  }
}
