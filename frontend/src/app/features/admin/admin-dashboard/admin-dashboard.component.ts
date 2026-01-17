import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MaterialModule } from '../../../shared/material.module';
import { ReportService, ReportResponse, ReportStatus } from '../../../core/services/report.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';
import { UpdateReportStatusDialogComponent } from '../update-report-status-dialog/update-report-status-dialog.component';

/**
 * AdminDashboardComponent - Main admin panel for managing reports
 * 
 * Features:
 * - View all reports in a clean table
 * - Filter reports by status (tabs)
 * - Update report status (Reviewed, Resolved, Dismissed)
 * - Add admin notes when updating status
 * - Real-time badge counts for each status
 * 
 * How it works:
 * 1. Loads all reports on init
 * 2. User clicks on a status tab to filter
 * 3. Admin can update report status with notes
 * 4. Table refreshes after each update
 */
@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, MaterialModule],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.scss'
})
export class AdminDashboardComponent implements OnInit {
  reports: ReportResponse[] = [];
  filteredReports: ReportResponse[] = [];
  isLoading = false;
  selectedStatus: 'ALL' | ReportStatus = 'ALL';
  
  // Status badge counts
  statusCounts = {
    PENDING: 0,
    REVIEWED: 0,
    RESOLVED: 0,
    DISMISSED: 0
  };

  // Table columns to display
  displayedColumns: string[] = ['id', 'reporter', 'type', 'reason', 'contentPreview', 'status', 'date', 'actions'];

  // Status options for tabs
  statuses: ('ALL' | ReportStatus)[] = ['ALL', ReportStatus.PENDING, ReportStatus.REVIEWED, ReportStatus.RESOLVED, ReportStatus.DISMISSED];

  constructor(
    private reportService: ReportService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.loadReports();
  }

  /**
   * Load all reports from backend
   */
  loadReports(): void {
    this.isLoading = true;
    
    this.reportService.getAllReports().subscribe({
      next: (reports) => {
        this.reports = reports;
        this.calculateStatusCounts();
        this.filterReports();
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading reports:', error);
        this.snackBar.open('Failed to load reports', 'Close', { duration: 3000 });
        this.isLoading = false;
      }
    });
  }

  /**
   * Calculate badge counts for each status
   */
  private calculateStatusCounts(): void {
    this.statusCounts = {
      PENDING: this.reports.filter(r => r.status === 'PENDING').length,
      REVIEWED: this.reports.filter(r => r.status === 'REVIEWED').length,
      RESOLVED: this.reports.filter(r => r.status === 'RESOLVED').length,
      DISMISSED: this.reports.filter(r => r.status === 'DISMISSED').length
    };
  }

  /**
   * Filter reports by selected status
   */
  filterReports(): void {
    if (this.selectedStatus === 'ALL') {
      this.filteredReports = [...this.reports];
    } else {
      this.filteredReports = this.reports.filter(r => r.status === this.selectedStatus);
    }
  }

  /**
   * Select a status filter tab
   */
  selectStatus(status: 'ALL' | ReportStatus): void {
    this.selectedStatus = status;
    this.filterReports();
  }

  /**
   * Open dialog to update report status
   */
  openUpdateStatusDialog(report: ReportResponse): void {
    const dialogRef = this.dialog.open(UpdateReportStatusDialogComponent, {
      width: '500px',
      data: {
        reportId: report.id,
        currentStatus: report.status,
        reportReason: report.reason
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === true) {
        // Reload reports after successful update
        this.loadReports();
        this.snackBar.open('Report status updated successfully', 'Close', { duration: 3000 });
      }
    });
  }

  /**
   * Get status badge color
   */
  getStatusColor(status: ReportStatus): string {
    switch (status) {
      case 'PENDING': return 'warn';
      case 'REVIEWED': return 'primary';
      case 'RESOLVED': return 'accent';
      case 'DISMISSED': return '';
      default: return '';
    }
  }

  /**
   * Get user-friendly reason label
   */
  getReasonLabel(reason: string): string {
    return this.reportService.getReasonLabel(reason as any);
  }

  /**
   * Format date to readable string
   */
  formatDate(date: string): string {
    return new Date(date).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  /**
   * Truncate long text for display
   */
  truncate(text: string, maxLength: number = 50): string {
    return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
  }
}
