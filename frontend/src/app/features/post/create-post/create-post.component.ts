import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { PostService } from '../../../core/services/post.service';
import { environment } from '../../../../environments/environment';

/**
 * CreatePostComponent - Form to create new blog posts
 * 
 * Features:
 * - Title field (required, 5-200 characters)
 * - Content field (required, 10-5000 characters)
 * - Category dropdown (required)
 * - Image upload with preview
 * - Form validation with error messages
 * - Loading state during submission
 * - Success/error notifications
 */
@Component({
  selector: 'app-create-post',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatSnackBarModule,
    MatIconModule,
    MatProgressBarModule
  ],
  templateUrl: './create-post.component.html',
  styleUrls: ['./create-post.component.scss']
})
export class CreatePostComponent implements OnInit {
  postForm!: FormGroup;
  isLoading = false;
  isUploading = false;
  selectedFile: File | null = null;
  imagePreview: string | null = null;
  uploadedImageUrl: string | null = null;

  // Available categories for blog posts
  categories = [
    'TECHNOLOGY',
    'HEALTH',
    'TRAVEL',
    'FOOD',
    'LIFESTYLE',
    'BUSINESS',
    'EDUCATION',
    'ENTERTAINMENT',
    'SPORTS',
    'OTHER'
  ];

  constructor(
    private fb: FormBuilder,
    private postService: PostService,
    private router: Router,
    private snackBar: MatSnackBar,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.initializeForm();
  }

  /**
   * Initialize the form with validation
   */
  private initializeForm(): void {
    this.postForm = this.fb.group({
      title: ['', [
        Validators.required,
        Validators.minLength(5),
        Validators.maxLength(200)
      ]],
      content: ['', [
        Validators.required,
        Validators.minLength(10),
        Validators.maxLength(5000)
      ]],
      category: ['', Validators.required]
    });
  }

  /**
   * Handle form submission
   */
  onSubmit(): void {
    // Check if form is valid
    if (this.postForm.invalid) {
      this.postForm.markAllAsTouched();
      return;
    }

    // Set loading state
    this.isLoading = true;

    // Prepare the post data
    const postData = {
      ...this.postForm.value,
      mediaUrl: this.uploadedImageUrl // Include the uploaded image URL
    };

    // Call PostService to create post
    this.postService.createPost(postData).subscribe({
      next: (response) => {
        // Success! Show message and redirect
        this.snackBar.open('Post created successfully!', 'Close', {
          duration: 3000,
          horizontalPosition: 'end',
          verticalPosition: 'top',
          panelClass: ['success-snackbar']
        });
        
        // Redirect to home page to see the new post
        this.router.navigate(['/home']);
      },
      error: (error) => {
        // Error! Show error message
        this.isLoading = false;
        const errorMessage = error.error?.message || 'Failed to create post. Please try again.';
        this.snackBar.open(errorMessage, 'Close', {
          duration: 5000,
          horizontalPosition: 'end',
          verticalPosition: 'top',
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  /**
   * Handle file selection
   */
  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      const file = input.files[0];
      
      // Validate file type
      if (!file.type.startsWith('image/')) {
        this.snackBar.open('Please select an image file', 'Close', {
          duration: 3000,
          panelClass: ['error-snackbar']
        });
        return;
      }

      // Validate file size (max 10MB)
      if (file.size > 10 * 1024 * 1024) {
        this.snackBar.open('File size must be less than 10MB', 'Close', {
          duration: 3000,
          panelClass: ['error-snackbar']
        });
        return;
      }

      this.selectedFile = file;
      
      // Create preview
      const reader = new FileReader();
      reader.onload = (e) => {
        this.imagePreview = e.target?.result as string;
      };
      reader.readAsDataURL(file);

      // Upload immediately
      this.uploadImage();
    }
  }

  /**
   * Upload image to server
   */
  private uploadImage(): void {
    if (!this.selectedFile) return;

    this.isUploading = true;
    const formData = new FormData();
    formData.append('file', this.selectedFile);

    this.http.post<{ url: string }>(`${environment.apiUrl}/upload`, formData)
      .subscribe({
        next: (response) => {
          this.uploadedImageUrl = response.url;
          this.isUploading = false;
          this.snackBar.open('Image uploaded successfully!', 'Close', {
            duration: 2000,
            panelClass: ['success-snackbar']
          });
        },
        error: (error) => {
          this.isUploading = false;
          this.imagePreview = null;
          this.selectedFile = null;
          const errorMessage = error.error?.error || 'Failed to upload image';
          this.snackBar.open(errorMessage, 'Close', {
            duration: 3000,
            panelClass: ['error-snackbar']
          });
        }
      });
  }

  /**
   * Remove selected image
   */
  removeImage(): void {
    this.selectedFile = null;
    this.imagePreview = null;
    this.uploadedImageUrl = null;
  }

  /**
   * Get error message for title field
   */
  getTitleError(): string {
    const titleControl = this.postForm.get('title');
    if (titleControl?.hasError('required')) {
      return 'Title is required';
    }
    if (titleControl?.hasError('minlength')) {
      return 'Title must be at least 5 characters';
    }
    if (titleControl?.hasError('maxlength')) {
      return 'Title cannot exceed 200 characters';
    }
    return '';
  }

  /**
   * Get error message for content field
   */
  getContentError(): string {
    const contentControl = this.postForm.get('content');
    if (contentControl?.hasError('required')) {
      return 'Content is required';
    }
    if (contentControl?.hasError('minlength')) {
      return 'Content must be at least 10 characters';
    }
    if (contentControl?.hasError('maxlength')) {
      return 'Content cannot exceed 5000 characters';
    }
    return '';
  }

  /**
   * Get error message for category field
   */
  getCategoryError(): string {
    const categoryControl = this.postForm.get('category');
    if (categoryControl?.hasError('required')) {
      return 'Please select a category';
    }
    return '';
  }

  /**
   * Cancel and go back to home
   */
  onCancel(): void {
    this.router.navigate(['/home']);
  }
}
