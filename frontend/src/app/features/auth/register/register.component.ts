import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { MaterialModule } from '../../../shared/material.module';
import { AuthService } from '../../../core/services/auth.service';
import { MatSnackBar } from '@angular/material/snack-bar';

/**
 * RegisterComponent - User registration page
 * 
 * What it does:
 * 1. Shows registration form (username, email, password, confirm password)
 * 2. Validates input (email format, password match, required fields)
 * 3. Calls AuthService to register
 * 4. On success: shows message and redirects to home
 * 5. On error: shows error message
 */

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, MaterialModule],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent {
  // Form group
  registerForm: FormGroup;
  
  // Loading state
  isLoading = false;
  
  // Password visibility toggles
  hidePassword = true;
  hideConfirmPassword = true;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    // Initialize form with validators
    this.registerForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(20)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]]
    }, {
      // Custom validator to check if passwords match
      validators: this.passwordMatchValidator
    });
  }

  /**
   * Custom validator to check if password and confirmPassword match
   */
  passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.get('password');
    const confirmPassword = control.get('confirmPassword');

    if (!password || !confirmPassword) {
      return null;
    }

    return password.value === confirmPassword.value ? null : { passwordMismatch: true };
  }

  /**
   * Handle form submission
   */
  onSubmit(): void {
    // Check if form is valid
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    // Set loading state
    this.isLoading = true;

    // Prepare request (exclude confirmPassword)
    const { confirmPassword, ...registerRequest } = this.registerForm.value;

    // Call AuthService to register
    this.authService.register(registerRequest).subscribe({
      next: (response) => {
        // Success! Show message and redirect
        this.snackBar.open(response.message || 'Registration successful!', 'Close', {
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
        const errorMessage = error.error?.message || 'Registration failed. Please try again.';
        
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
   * Get error message for username field
   */
  getUsernameErrorMessage(): string {
    const usernameControl = this.registerForm.get('username');
    
    if (usernameControl?.hasError('required')) {
      return 'Username is required';
    }
    
    if (usernameControl?.hasError('minlength')) {
      return 'Username must be at least 3 characters';
    }
    
    if (usernameControl?.hasError('maxlength')) {
      return 'Username must be less than 20 characters';
    }
    
    return '';
  }

  /**
   * Get error message for email field
   */
  getEmailErrorMessage(): string {
    const emailControl = this.registerForm.get('email');
    
    if (emailControl?.hasError('required')) {
      return 'Email is required';
    }
    
    if (emailControl?.hasError('email')) {
      return 'Please enter a valid email';
    }
    
    return '';
  }

  /**
   * Get error message for password field
   */
  getPasswordErrorMessage(): string {
    const passwordControl = this.registerForm.get('password');
    
    if (passwordControl?.hasError('required')) {
      return 'Password is required';
    }
    
    if (passwordControl?.hasError('minlength')) {
      return 'Password must be at least 6 characters';
    }
    
    return '';
  }

  /**
   * Get error message for confirm password field
   */
  getConfirmPasswordErrorMessage(): string {
    const confirmPasswordControl = this.registerForm.get('confirmPassword');
    
    if (confirmPasswordControl?.hasError('required')) {
      return 'Please confirm your password';
    }
    
    if (this.registerForm.hasError('passwordMismatch') && confirmPasswordControl?.touched) {
      return 'Passwords do not match';
    }
    
    return '';
  }
}
