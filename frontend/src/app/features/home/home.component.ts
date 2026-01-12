import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MaterialModule } from '../../shared/material.module';
import { PostService } from '../../core/services/post.service';
import { Post } from '../../core/models/post.model';
import { PostCardComponent } from '../post/post-card/post-card.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';

/**
 * HomeComponent - Main homepage showing post feed
 * 
 * What it does:
 * 1. Fetches posts from backend with pagination
 * 2. Displays posts in a feed
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

  constructor(private postService: PostService) {}

  ngOnInit(): void {
    // Load posts when component initializes
    this.loadPosts();
  }

  /**
   * Load posts from backend
   */
  loadPosts(): void {
    this.isLoading = true;

    this.postService.getAllPosts(this.currentPage, this.pageSize, 'createdAt').subscribe({
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
