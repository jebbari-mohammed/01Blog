import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MaterialModule } from '../../material.module';
import { ReportService, ReportType, ReportReason } from '../../../core/services/report.service';
import { MatSnackBar } from '@angular/material/snack-bar';

/**
 * ReportDialogComponent - Simple dialog for reporting content
 * 
 * How it works:
 * 1. User clicks "Report" button on a post/comment
 * 2. This dialog opens
 * 3. User selects a reason from dropdown
 * 4. User can add optional description
 * 5. Submit → API call → Success message
 */

export interface ReportDialogData {
  contentType: ReportType;  // POST or COMMENT
  contentId: number;         // ID of the post or comment
  contentPreview: string;    // Show user what they're reporting
}

@Component({
  selector: 'app-report-dialog',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MaterialModule],
  templateUrl: './report-dialog.component.html',
  styleUrls: ['./report-dialog.component.scss']
})
export class ReportDialogComponent {
  reportForm: FormGroup;
  isSubmitting = false;
  reasons: { value: ReportReason; label: string }[];

  constructor(
    public dialogRef: MatDialogRef<ReportDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ReportDialogData,
    private fb: FormBuilder,
    private reportService: ReportService,
    private snackBar: MatSnackBar
  ) {
    // Get all available report reasons
    this.reasons = this.reportService.getAllReasons();

    // Create form with validation
    this.reportForm = this.fb.group({
      reason: ['', Validators.required],  // Required: must select a reason
      description: ['']                    // Optional: additional details
    });
  }

  /**
   * Submit the report
   */
  onSubmit(): void {
    if (this.reportForm.invalid || this.isSubmitting) return;

    this.isSubmitting = true;

    // Prepare request
    const request = {
      reportType: this.data.contentType,
      contentId: this.data.contentId,
      reason: this.reportForm.value.reason,
      description: this.reportForm.value.description || undefined
    };

    // Call API
    this.reportService.createReport(request).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.snackBar.open('Report submitted successfully. Thank you!', 'Close', {
          duration: 4000
        });
        this.dialogRef.close(true); // Close dialog and indicate success
      },
      error: (error) => {
        this.isSubmitting = false;
        console.error('Error submitting report:', error);
        
        // Show appropriate error message
        let message = 'Failed to submit report. Please try again.';
        if (error.status === 400) {
          message = 'You have already reported this content.';
        }
        
        this.snackBar.open(message, 'Close', {
          duration: 4000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  /**
   * Cancel and close dialog
   */
  onCancel(): void {
    this.dialogRef.close(false);
  }

  /**
   * Get content type label for display
   */
  getContentTypeLabel(): string {
    return this.data.contentType === ReportType.POST ? 'Post' : 'Comment';
  }
}
