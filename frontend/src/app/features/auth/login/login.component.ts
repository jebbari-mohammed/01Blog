import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { MaterialModule } from '../../../shared/material.module';
import { AuthService } from '../../../core/services/auth.service';
import { MatSnackBar } from '@angular/material/snack-bar';

/**
 * LoginComponent - User login page
 * 
 * What it does:
 * 1. Shows email/password form
 * 2. Validates input (email format, required fields)
 * 3. Calls AuthService to login
 * 4. On success: redirects to home page
 * 5. On error: shows error message
 */

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, MaterialModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  // Form group - manages form state and validation
  loginForm: FormGroup;
  
  // Loading state - shows spinner during API call
  isLoading = false;
  
  // Password visibility toggle
  hidePassword = true;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    // Initialize form with validators
    this.loginForm = this.fb.group({
      identifier: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  /**
   * Handle form submission
   */
  onSubmit(): void {
    // Check if form is valid
    if (this.loginForm.invalid) {
      // Mark all fields as touched to show validation errors
      this.loginForm.markAllAsTouched();
      return;
    }

    // Set loading state
    this.isLoading = true;

    // Call AuthService to login
    this.authService.login(this.loginForm.value).subscribe({
      next: (response) => {
        // Success! Show message and redirect
        this.snackBar.open(response.message || 'Login successful!', 'Close', {
          duration: 3000,
          horizontalPosition: 'end',
          verticalPosition: 'top'
        });
        
        // Redirect to home page
        this.router.navigate(['/home']);
      },
      error: (error) => {
        // Error! Show error message
        this.isLoading = false;
        const errorMessage = error.error?.message || 'Login failed. Please try again.';
        
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
   * Get error message for identifier field (email or username)
   */
  getEmailErrorMessage(): string {
    const identifierControl = this.loginForm.get('identifier');
    
    if (identifierControl?.hasError('required')) {
      return 'Email or username is required';
    }
    
    if (identifierControl?.hasError('minlength')) {
      return 'Must be at least 3 characters';
    }
    
    return '';
  }

  /**
   * Get error message for password field
   */
  getPasswordErrorMessage(): string {
    const passwordControl = this.loginForm.get('password');
    
    if (passwordControl?.hasError('required')) {
      return 'Password is required';
    }
    
    if (passwordControl?.hasError('minlength')) {
      return 'Password must be at least 6 characters';
    }
    
    return '';
  }
}
