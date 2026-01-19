import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { PostService } from '../../../core/services/post.service';
import { Post } from '../../../core/models/post.model';
import { PostCardComponent } from '../post-card/post-card.component';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-post-detail',
  standalone: true,
  imports: [CommonModule, PostCardComponent, MatProgressSpinnerModule],
  templateUrl: './post-detail.component.html',
  styleUrl: './post-detail.component.scss'
})
export class PostDetailComponent implements OnInit {
  post: Post | null = null;
  isLoading = true;
  error: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private postService: PostService
  ) {}

  ngOnInit(): void {
    // Get post ID from route parameter
    const postId = this.route.snapshot.paramMap.get('id');
    
    if (postId) {
      this.loadPost(Number(postId));
    } else {
      this.error = 'No post ID provided';
      this.isLoading = false;
    }
  }

  loadPost(postId: number): void {
    this.postService.getPostById(postId).subscribe({
      next: (post) => {
        this.post = post;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading post:', error);
        this.error = 'Post not found';
        this.isLoading = false;
      }
    });
  }

  onPostDeleted(): void {
    // Navigate back to home if post is deleted
    this.router.navigate(['/home']);
  }

  goBack(): void {
    // Navigate back to home
    this.router.navigate(['/home']);
  }
}
