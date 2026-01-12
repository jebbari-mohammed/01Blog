import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Comment } from '../../../core/models/comment.model';
import { CommentService } from '../../../core/services/comment.service';
import { AuthService } from '../../../core/services/auth.service';

/**
 * CommentsComponent - Display and manage comments for a post
 * 
 * Features:
 * - Show list of comments with author and timestamp
 * - Add new comment form (for logged-in users)
 * - Edit own comments
 * - Delete own comments
 * - Real-time comment count
 */
@Component({
  selector: 'app-comments',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSnackBarModule,
    MatDividerModule,
    MatProgressSpinnerModule,
    MatTooltipModule
  ],
  templateUrl: './comments.component.html',
  styleUrls: ['./comments.component.scss']
})
export class CommentsComponent implements OnInit {
  @Input() postId!: number;
  
  comments: Comment[] = [];
  commentForm!: FormGroup;
  isLoading = false;
  isSubmitting = false;
  editingCommentId: number | null = null;
  showComments = false;

  constructor(
    private fb: FormBuilder,
    private commentService: CommentService,
    public authService: AuthService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.initializeForm();
  }

  /**
   * Initialize comment form
   */
  private initializeForm(): void {
    this.commentForm = this.fb.group({
      text: ['', [
        Validators.required,
        Validators.minLength(1),
        Validators.maxLength(500)
      ]]
    });
  }

  /**
   * Toggle comments visibility and load if needed
   */
  toggleComments(): void {
    this.showComments = !this.showComments;
    
    if (this.showComments && this.comments.length === 0) {
      this.loadComments();
    }
  }

  /**
   * Load comments for this post
   */
  private loadComments(): void {
    this.isLoading = true;
    
    this.commentService.getComments(this.postId).subscribe({
      next: (comments: Comment[]) => {
        this.comments = comments;
        this.isLoading = false;
      },
      error: (error: any) => {
        console.error('Error loading comments:', error);
        this.isLoading = false;
        this.snackBar.open('Failed to load comments', 'Close', {
          duration: 3000
        });
      }
    });
  }

  /**
   * Submit new comment or update existing
   */
  onSubmit(): void {
    if (this.commentForm.invalid) {
      return;
    }

    if (!this.authService.isLoggedIn()) {
      this.snackBar.open('Please login to comment', 'Close', {
        duration: 3000
      });
      return;
    }

    this.isSubmitting = true;

    if (this.editingCommentId) {
      // Update existing comment
      this.updateComment();
    } else {
      // Create new comment
      this.createComment();
    }
  }

  /**
   * Create new comment
   */
  private createComment(): void {
    const text = this.commentForm.get('text')?.value;
    
    this.commentService.addComment(this.postId, { text }).subscribe({
      next: (comment: Comment) => {
        this.comments.unshift(comment); // Add to beginning of array
        this.commentForm.reset();
        this.isSubmitting = false;
        this.snackBar.open('Comment added!', 'Close', {
          duration: 2000
        });
      },
      error: (error: any) => {
        console.error('Error creating comment:', error);
        this.isSubmitting = false;
        this.snackBar.open('Failed to add comment', 'Close', {
          duration: 3000
        });
      }
    });
  }

  /**
   * Update existing comment
   */
  private updateComment(): void {
    if (!this.editingCommentId) return;

    const text = this.commentForm.get('text')?.value;
    
    this.commentService.updateComment(this.editingCommentId, { text }).subscribe({
      next: (updatedComment: Comment) => {
        // Find and update the comment in array
        const index = this.comments.findIndex(c => c.id === this.editingCommentId);
        if (index !== -1) {
          this.comments[index] = updatedComment;
        }
        
        this.cancelEdit();
        this.isSubmitting = false;
        this.snackBar.open('Comment updated!', 'Close', {
          duration: 2000
        });
      },
      error: (error: any) => {
        console.error('Error updating comment:', error);
        this.isSubmitting = false;
        this.snackBar.open('Failed to update comment', 'Close', {
          duration: 3000
        });
      }
    });
  }

  /**
   * Start editing a comment
   */
  editComment(comment: Comment): void {
    this.editingCommentId = comment.id;
    this.commentForm.patchValue({ text: comment.text });
  }

  /**
   * Cancel editing
   */
  cancelEdit(): void {
    this.editingCommentId = null;
    this.commentForm.reset();
  }

  /**
   * Delete a comment
   */
  deleteComment(commentId: number): void {
    const confirmed = confirm('Are you sure you want to delete this comment?');
    
    if (!confirmed) {
      return;
    }

    this.commentService.deleteComment(commentId).subscribe({
      next: () => {
        this.comments = this.comments.filter(c => c.id !== commentId);
        this.snackBar.open('Comment deleted!', 'Close', {
          duration: 2000
        });
      },
      error: (error) => {
        console.error('Error deleting comment:', error);
        this.snackBar.open('Failed to delete comment', 'Close', {
          duration: 3000
        });
      }
    });
  }

  /**
   * Check if current user can edit/delete comment
   */
  canEditComment(comment: Comment): boolean {
    const currentUser = this.authService.getCurrentUser();
    return currentUser?.username === comment.username;
  }

  /**
   * Format date to relative time (e.g., "2 hours ago")
   */
  formatDate(dateString: string): string {
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMins / 60);
    const diffDays = Math.floor(diffHours / 24);

    if (diffMins < 1) return 'just now';
    if (diffMins < 60) return `${diffMins} minute${diffMins > 1 ? 's' : ''} ago`;
    if (diffHours < 24) return `${diffHours} hour${diffHours > 1 ? 's' : ''} ago`;
    if (diffDays < 7) return `${diffDays} day${diffDays > 1 ? 's' : ''} ago`;
    
    return date.toLocaleDateString();
  }
}
