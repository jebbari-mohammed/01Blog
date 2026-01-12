import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { MaterialModule } from '../../../shared/material.module';
import { UserService } from '../../../core/services/user.service';
import { PostService } from '../../../core/services/post.service';
import { AuthService } from '../../../core/services/auth.service';
import { UserProfileResponse } from '../../../core/models/user.model';
import { Post } from '../../../core/models/post.model';
import { PostCardComponent } from '../../post/post-card/post-card.component';
import { MatSnackBar } from '@angular/material/snack-bar';

/**
 * ProfileComponent - Display user profile with their posts
 * 
 * Features:
 * - User info (avatar, bio, stats)
 * - User's posts feed
 * - Edit profile button (if own profile)
 * - Follow/Unfollow button (if not own profile)
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

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private userService: UserService,
    private postService: PostService,
    public authService: AuthService,
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
        this.profile = profile;
        this.isLoading = false;
        
        // Check if this is the current user's profile
        const currentUser = this.authService.getCurrentUser();
        this.isOwnProfile = currentUser?.username === this.username;
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
   * Navigate to edit profile
   */
  editProfile(): void {
    this.router.navigate(['/profile', this.username, 'edit']);
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
}
