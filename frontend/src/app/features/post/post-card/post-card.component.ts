import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MaterialModule } from '../../../shared/material.module';
import { Post } from '../../../core/models/post.model';
import { LikeService } from '../../../core/services/like.service';
import { PostService } from '../../../core/services/post.service';
import { AuthService } from '../../../core/services/auth.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';
import { CommentsComponent } from '../comments/comments.component';
import { environment } from '../../../../environments/environment';

/**
 * PostCardComponent - Displays a single post
 * 
 * What it shows:
 * 1. Post title, content, category
 * 2. Author username and creation date
 * 3. Like button with count
 * 4. Comment button with count
 * 5. Delete button (if user is author or admin)
 * 6. Post image (if exists)
 */

@Component({
  selector: 'app-post-card',
  standalone: true,
  imports: [CommonModule, RouterModule, MaterialModule, CommentsComponent],
  templateUrl: './post-card.component.html',
  styleUrl: './post-card.component.scss'
})
export class PostCardComponent {
  // Post data input from parent component
  @Input() post!: Post;
  
  // Event emitter for post deletion
  @Output() postDeleted = new EventEmitter<number>();
  
  // Loading states
  isLiking = false;
  isDeleting = false;

  constructor(
    private likeService: LikeService,
    private postService: PostService,
    public authService: AuthService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}
  
  ngOnInit() {
    console.log('PostCard initialized with post:', {
      id: this.post.id,
      title: this.post.title,
      isLikedByCurrentUser: this.post.isLikedByCurrentUser,
      likeCount: this.post.likeCount,
      mediaUrl: this.post.mediaUrl
    });
    
    // Convert relative media URL to absolute URL
    if (this.post.mediaUrl && !this.post.mediaUrl.startsWith('http')) {
      // Remove /api from the base URL to get http://localhost:8080
      const baseUrl = environment.apiUrl.replace('/api', '');
      this.post.mediaUrl = `${baseUrl}${this.post.mediaUrl}`;
      console.log('Converted media URL to:', this.post.mediaUrl);
    }
  }

  /**
   * Toggle like on post
   */
  toggleLike(): void {
    // Check if user is logged in
    if (!this.authService.isLoggedIn()) {
      this.snackBar.open('Please login to like posts', 'Close', {
        duration: 3000
      });
      return;
    }

    this.isLiking = true;

    if (this.post.isLikedByCurrentUser) {
      // Unlike the post
      this.likeService.unlikePost(this.post.id).subscribe({
        next: () => {
          this.post.isLikedByCurrentUser = false;
          this.post.likeCount = Math.max(0, this.post.likeCount - 1);
          this.isLiking = false;
        },
        error: (error) => {
          console.error('Error unliking post:', error);
          this.isLiking = false;
          // If already unliked, just update the UI
          if (error.status === 404 || error.status === 409) {
            this.post.isLikedByCurrentUser = false;
            this.post.likeCount = Math.max(0, this.post.likeCount - 1);
          }
        }
      });
    } else {
      // Like the post
      this.likeService.likePost(this.post.id).subscribe({
        next: (response) => {
          this.post.isLikedByCurrentUser = true;
          this.post.likeCount++;
          this.isLiking = false;
        },
        error: (error) => {
          console.error('Error liking post:', error);
          this.isLiking = false;
          // If already liked (409 conflict), just update the UI
          if (error.status === 409) {
            this.post.isLikedByCurrentUser = true;
            this.snackBar.open('You already liked this post', 'Close', {
              duration: 2000
            });
          } else {
            this.snackBar.open('Failed to like post', 'Close', {
              duration: 3000
            });
          }
        }
      });
    }
  }

  /**
   * Delete post (with confirmation)
   */
  deletePost(): void {
    const confirmed = confirm('Are you sure you want to delete this post?');
    
    if (!confirmed) {
      return;
    }

    this.isDeleting = true;

    this.postService.deletePost(this.post.id).subscribe({
      next: () => {
        this.snackBar.open('Post deleted successfully', 'Close', {
          duration: 3000
        });
        
        // Emit event to parent component
        this.postDeleted.emit(this.post.id);
      },
      error: (error) => {
        const errorMessage = error.error?.message || 'Error deleting post';
        this.snackBar.open(errorMessage, 'Close', {
          duration: 3000
        });
        this.isDeleting = false;
      }
    });
  }

  /**
   * Check if current user can delete this post
   */
  canDelete(): boolean {
    const currentUser = this.authService.getCurrentUser();
    
    if (!currentUser) {
      return false;
    }

    // User can delete if they are the author or an admin
    return currentUser.username === this.post.authorUsername || currentUser.role === 'ADMIN';
  }

  /**
   * Format date to readable string
   */
  formatDate(date: Date | string): string {
    const d = new Date(date);
    const now = new Date();
    const diffMs = now.getTime() - d.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins} minute${diffMins > 1 ? 's' : ''} ago`;
    if (diffHours < 24) return `${diffHours} hour${diffHours > 1 ? 's' : ''} ago`;
    if (diffDays < 7) return `${diffDays} day${diffDays > 1 ? 's' : ''} ago`;
    
    return d.toLocaleDateString();
  }

  /**
   * Handle image load error
   */
  onImageError(event: Event): void {
    console.error('Failed to load image:', this.post.mediaUrl);
    const img = event.target as HTMLImageElement;
    console.error('Image src:', img.src);
    console.error('Image natural width:', img.naturalWidth);
  }

  /**
   * Handle image load success
   */
  onImageLoad(): void {
    console.log('Image loaded successfully:', this.post.mediaUrl);
  }
}
