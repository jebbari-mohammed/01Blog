import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MaterialModule } from '../../shared/material.module';
import { PostService } from '../../core/services/post.service';
import { AuthService } from '../../core/services/auth.service';
import { Post } from '../../core/models/post.model';
import { PostCardComponent } from '../post/post-card/post-card.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';

/**
 * HomeComponent - Main homepage showing post feed
 * 
 * Features:
 * 1. Fetches posts from backend with pagination
 * 2. Two feed modes:
 *    - Following: Shows posts from users you follow (personalized feed)
 *    - Discover: Shows all posts (public feed)
 * 3. Handles pagination (load more)
 * 4. Shows loading state
 * 5. Shows "Create Post" button for logged-in users
 */

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    CommonModule, 
    RouterModule, 
    MaterialModule, 
    PostCardComponent,
    LoadingSpinnerComponent
  ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent implements OnInit {
  // Array of posts
  posts: Post[] = [];
  
  // Loading state
  isLoading = false;
  
  // Pagination
  currentPage = 0;
  pageSize = 10;
  totalPages = 0;
  hasMorePosts = true;

  // Feed mode: 'following' or 'discover'
  feedMode: 'following' | 'discover' = 'following';

  constructor(
    private postService: PostService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    // Load posts when component initializes
    // If user is logged in, show following feed by default
    // If user is not logged in, show discover feed
    if (!this.authService.isLoggedIn()) {
      this.feedMode = 'discover';
    }
    this.loadPosts();
  }

  /**
   * Load posts from backend based on current feed mode
   */
  loadPosts(): void {
    this.isLoading = true;

    // Choose which endpoint to call based on feed mode
    const postsObservable = this.feedMode === 'following'
      ? this.postService.getFeedPosts(this.currentPage, this.pageSize, 'createdAt')
      : this.postService.getAllPosts(this.currentPage, this.pageSize, 'createdAt');

    postsObservable.subscribe({
      next: (response) => {
        // Add new posts to existing array
        this.posts = [...this.posts, ...response.content];
        
        // Update pagination info
        this.totalPages = response.totalPages;
        this.hasMorePosts = this.currentPage < response.totalPages - 1;
        
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading posts:', error);
        this.isLoading = false;
      }
    });
  }

  /**
   * Switch between Following and Discover feeds
   */
  switchFeed(mode: 'following' | 'discover'): void {
    if (this.feedMode === mode) return; // Already on this feed
    
    this.feedMode = mode;
    this.refreshPosts();
  }

  /**
   * Load more posts (next page)
   */
  loadMorePosts(): void {
    if (this.hasMorePosts && !this.isLoading) {
      this.currentPage++;
      this.loadPosts();
    }
  }

  /**
   * Refresh posts (reload from first page)
   */
  refreshPosts(): void {
    this.posts = [];
    this.currentPage = 0;
    this.loadPosts();
  }

  /**
   * Handle post deletion (called from PostCard)
   */
  onPostDeleted(postId: number): void {
    // Remove post from array
    this.posts = this.posts.filter(post => post.id !== postId);
  }
}
