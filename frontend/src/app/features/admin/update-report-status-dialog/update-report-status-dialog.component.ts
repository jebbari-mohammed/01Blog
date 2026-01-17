import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MaterialModule } from '../../../shared/material.module';
import { ReportService, ReportStatus } from '../../../core/services/report.service';
import { MatSnackBar } from '@angular/material/snack-bar';

/**
 * Data interface for the dialog
 */
export interface UpdateReportStatusDialogData {
  reportId: number;
  currentStatus: ReportStatus;
  reportReason: string; // Comes from backend as string
}

/**
 * UpdateReportStatusDialogComponent - Dialog for admins to update report status
 * 
 * How it works:
 * 1. Admin selects new status (REVIEWED, RESOLVED, DISMISSED)
 * 2. Admin adds optional notes explaining the decision
 * 3. On submit, calls reportService.updateReportStatus()
 * 4. Shows success/error message
 */
@Component({
  selector: 'app-update-report-status-dialog',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MaterialModule, MatDialogModule],
  templateUrl: './update-report-status-dialog.component.html',
  styleUrl: './update-report-status-dialog.component.scss'
})
export class UpdateReportStatusDialogComponent {
  updateForm: FormGroup;
  isSubmitting = false;

  // Available status options (excluding PENDING as it's the default)
  statusOptions: ReportStatus[] = [
    ReportStatus.REVIEWED,
    ReportStatus.RESOLVED,
    ReportStatus.DISMISSED
  ];

  constructor(
    private fb: FormBuilder,
    private reportService: ReportService,
    private snackBar: MatSnackBar,
    private dialogRef: MatDialogRef<UpdateReportStatusDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: UpdateReportStatusDialogData
  ) {
    // Initialize form with current status
    this.updateForm = this.fb.group({
      status: [data.currentStatus, Validators.required],
      adminNotes: ['', Validators.maxLength(1000)]
    });
  }

  /**
   * Submit status update
   */
  onSubmit(): void {
    if (this.updateForm.invalid || this.isSubmitting) {
      return;
    }

    this.isSubmitting = true;

    const { status, adminNotes } = this.updateForm.value;

    this.reportService.updateReportStatus(this.data.reportId, {
      status,
      adminNotes: adminNotes || undefined // Send undefined if empty
    }).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.dialogRef.close(true); // Close with success
      },
      error: (error) => {
        console.error('Error updating report status:', error);
        this.isSubmitting = false;
        
        const errorMessage = error.error?.message || 'Failed to update report status';
        this.snackBar.open(errorMessage, 'Close', {
          duration: 3000
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
   * Get user-friendly status label
   */
  getStatusLabel(status: ReportStatus): string {
    const labels: Record<ReportStatus, string> = {
      'PENDING': 'Pending Review',
      'REVIEWED': 'Reviewed (Under Investigation)',
      'RESOLVED': 'Resolved (Action Taken)',
      'DISMISSED': 'Dismissed (No Action Needed)'
    };
    return labels[status] || status;
  }

  /**
   * Get reason label
   */
  getReasonLabel(): string {
    return this.reportService.getReasonLabel(this.data.reportReason as any);
  }
}
