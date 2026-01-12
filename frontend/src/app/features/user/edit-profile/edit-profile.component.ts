import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { MaterialModule } from '../../../shared/material.module';
import { UserService } from '../../../core/services/user.service';
import { AuthService } from '../../../core/services/auth.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { environment } from '../../../../environments/environment';

/**
 * EditProfileComponent - Edit user profile
 * 
 * Features:
 * - Update username
 * - Update bio
 * - Upload profile picture
 * - Upload cover image
 * - Preview images before upload
 */
@Component({
  selector: 'app-edit-profile',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MaterialModule
  ],
  templateUrl: './edit-profile.component.html',
  styleUrls: ['./edit-profile.component.scss']
})
export class EditProfileComponent implements OnInit {
  profileForm!: FormGroup;
  isLoading = false;
  isUploadingProfile = false;
  isUploadingCover = false;
  
  // Image previews
  profileImagePreview: string | null = null;
  coverImagePreview: string | null = null;
  
  // Uploaded URLs
  profileImageUrl: string | null = null;
  coverImageUrl: string | null = null;
  
  currentUsername: string = '';

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private authService: AuthService,
    private router: Router,
    private http: HttpClient,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.initializeForm();
    this.loadCurrentProfile();
  }

  /**
   * Initialize form
   */
  private initializeForm(): void {
    this.profileForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(20)]],
      bio: ['', [Validators.maxLength(500)]]
    });
  }

  /**
   * Load current user profile
   */
  private loadCurrentProfile(): void {
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) {
      this.router.navigate(['/auth/login']);
      return;
    }

    this.currentUsername = currentUser.username;
    
    this.userService.getUserProfile(currentUser.username).subscribe({
      next: (profile) => {
        console.log('Loaded profile:', profile);
        
        this.profileForm.patchValue({
          username: profile.username,
          bio: profile.bio || ''
        });
        
        // Set existing images
        if (profile.profilePicture) {
          const fullUrl = this.getFullImageUrl(profile.profilePicture);
          console.log('Profile picture URL:', fullUrl);
          this.profileImagePreview = fullUrl;
          this.profileImageUrl = profile.profilePicture;
          console.log('profileImagePreview set to:', this.profileImagePreview);
        }
        
        if (profile.coverImage) {
          const fullUrl = this.getFullImageUrl(profile.coverImage);
          console.log('Cover image URL:', fullUrl);
          this.coverImagePreview = fullUrl;
          this.coverImageUrl = profile.coverImage;
          console.log('coverImagePreview set to:', this.coverImagePreview);
        }
      },
      error: (error) => {
        console.error('Error loading profile:', error);
        this.snackBar.open('Failed to load profile', 'Close', {
          duration: 3000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  /**
   * Handle profile image selection
   */
  onProfileImageSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      const file = input.files[0];
      
      if (!this.validateImage(file)) return;
      
      // Create preview
      const reader = new FileReader();
      reader.onload = (e) => {
        this.profileImagePreview = e.target?.result as string;
      };
      reader.readAsDataURL(file);

      // Upload immediately
      this.uploadImage(file, 'profile');
    }
  }

  /**
   * Handle cover image selection
   */
  onCoverImageSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      const file = input.files[0];
      
      if (!this.validateImage(file)) return;
      
      // Create preview
      const reader = new FileReader();
      reader.onload = (e) => {
        this.coverImagePreview = e.target?.result as string;
      };
      reader.readAsDataURL(file);

      // Upload immediately
      this.uploadImage(file, 'cover');
    }
  }

  /**
   * Validate image file
   */
  private validateImage(file: File): boolean {
    if (!file.type.startsWith('image/')) {
      this.snackBar.open('Please select an image file', 'Close', {
        duration: 3000,
        panelClass: ['error-snackbar']
      });
      return false;
    }

    if (file.size > 10 * 1024 * 1024) {
      this.snackBar.open('File size must be less than 10MB', 'Close', {
        duration: 3000,
        panelClass: ['error-snackbar']
      });
      return false;
    }

    return true;
  }

  /**
   * Upload image to server
   */
  private uploadImage(file: File, type: 'profile' | 'cover'): void {
    if (type === 'profile') {
      this.isUploadingProfile = true;
    } else {
      this.isUploadingCover = true;
    }

    const formData = new FormData();
    formData.append('file', file);

    this.http.post<{ url: string }>(`${environment.apiUrl}/upload`, formData)
      .subscribe({
        next: (response) => {
          if (type === 'profile') {
            this.profileImageUrl = response.url;
            this.isUploadingProfile = false;
          } else {
            this.coverImageUrl = response.url;
            this.isUploadingCover = false;
          }
          
          this.snackBar.open('Image uploaded successfully!', 'Close', {
            duration: 2000,
            panelClass: ['success-snackbar']
          });
        },
        error: (error) => {
          if (type === 'profile') {
            this.isUploadingProfile = false;
            this.profileImagePreview = null;
          } else {
            this.isUploadingCover = false;
            this.coverImagePreview = null;
          }
          
          const errorMessage = error.error?.error || 'Failed to upload image';
          this.snackBar.open(errorMessage, 'Close', {
            duration: 3000,
            panelClass: ['error-snackbar']
          });
        }
      });
  }

  /**
   * Remove profile image
   */
  removeProfileImage(): void {
    this.profileImagePreview = null;
    this.profileImageUrl = null;
  }

  /**
   * Remove cover image
   */
  removeCoverImage(): void {
    this.coverImagePreview = null;
    this.coverImageUrl = null;
  }

  /**
   * Submit form
   */
  onSubmit(): void {
    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;

    const updateData: any = {
      username: this.profileForm.value.username,
      bio: this.profileForm.value.bio
    };
    
    if (this.profileImageUrl) {
      updateData.profilePicture = this.profileImageUrl;
    }
    
    if (this.coverImageUrl) {
      updateData.coverImage = this.coverImageUrl;
    }

    this.userService.updateProfile(updateData).subscribe({
      next: (response) => {
        this.snackBar.open('Profile updated successfully!', 'Close', {
          duration: 3000,
          panelClass: ['success-snackbar']
        });
        
        // Navigate to profile page
        this.router.navigate(['/profile', response.username]);
      },
      error: (error) => {
        this.isLoading = false;
        const errorMessage = error.error?.message || 'Failed to update profile';
        this.snackBar.open(errorMessage, 'Close', {
          duration: 3000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  /**
   * Cancel editing
   */
  onCancel(): void {
    this.router.navigate(['/profile', this.currentUsername]);
  }

  /**
   * Get full image URL
   */
  private getFullImageUrl(url: string): string {
    if (url.startsWith('http')) return url;
    return `${environment.apiUrl.replace('/api', '')}${url}`;
  }

  /**
   * Get username error message
   */
  getUsernameError(): string {
    const control = this.profileForm.get('username');
    if (control?.hasError('required')) return 'Username is required';
    if (control?.hasError('minlength')) return 'Username must be at least 3 characters';
    if (control?.hasError('maxlength')) return 'Username cannot exceed 20 characters';
    return '';
  }

  /**
   * Get bio error message
   */
  getBioError(): string {
    const control = this.profileForm.get('bio');
    if (control?.hasError('maxlength')) return 'Bio cannot exceed 500 characters';
    return '';
  }

  /**
   * Handle image load success
   */
  onImageLoad(type: string): void {
    console.log(`${type} image loaded successfully`);
  }

  /**
   * Handle image load error
   */
  onImageError(type: string, event: any): void {
    console.error(`Failed to load ${type} image:`, event);
    console.error(`Image src:`, event.target?.src);
  }
}
