import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

/**
 * ReportService - Handle all report-related API calls
 * 
 * User operations:
 * - Report posts/comments
 * 
 * Admin operations:
 * - View all reports
 * - Filter reports by status
 * - Update report status
 * - Get pending reports count
 */

export enum ReportType {
  POST = 'POST',
  COMMENT = 'COMMENT',
  USER = 'USER'
}

export enum ReportReason {
  SPAM = 'SPAM',
  HARASSMENT = 'HARASSMENT',
  HATE_SPEECH = 'HATE_SPEECH',
  VIOLENCE = 'VIOLENCE',
  SEXUAL_CONTENT = 'SEXUAL_CONTENT',
  FALSE_INFORMATION = 'FALSE_INFORMATION',
  COPYRIGHT = 'COPYRIGHT',
  OTHER = 'OTHER'
}

export enum ReportStatus {
  PENDING = 'PENDING',
  REVIEWED = 'REVIEWED',
  RESOLVED = 'RESOLVED',
  DISMISSED = 'DISMISSED'
}

export interface CreateReportRequest {
  reportType: ReportType;
  contentId: number;
  reason: ReportReason;
  description?: string;
}

export interface ReportResponse {
  id: number;
  reportType: ReportType;
  reason: ReportReason;
  description: string;
  status: ReportStatus;
  reporterId: number;
  reporterUsername: string;
  contentId: number;
  contentPreview: string;
  contentAuthor: string;
  reviewedById?: number;
  reviewedByUsername?: string;
  adminNotes?: string;
  reviewedAt?: string;
  createdAt: string;
}

export interface UpdateReportStatusRequest {
  status: ReportStatus;
  adminNotes?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ReportService {
  private apiUrl = `${environment.apiUrl}/reports`;

  constructor(private http: HttpClient) { }

  /**
   * Create a new report (user)
   * POST /api/reports
   */
  createReport(request: CreateReportRequest): Observable<ReportResponse> {
    return this.http.post<ReportResponse>(this.apiUrl, request);
  }

  /**
   * Get all reports (admin only)
   * GET /api/reports
   */
  getAllReports(): Observable<ReportResponse[]> {
    return this.http.get<ReportResponse[]>(this.apiUrl);
  }

  /**
   * Get reports by status (admin only)
   * GET /api/reports/status/{status}
   */
  getReportsByStatus(status: ReportStatus): Observable<ReportResponse[]> {
    return this.http.get<ReportResponse[]>(`${this.apiUrl}/status/${status}`);
  }

  /**
   * Get pending reports (admin only)
   * GET /api/reports/pending
   */
  getPendingReports(): Observable<ReportResponse[]> {
    return this.http.get<ReportResponse[]>(`${this.apiUrl}/pending`);
  }

  /**
   * Get count of pending reports (admin only)
   * GET /api/reports/count/pending
   */
  getPendingReportsCount(): Observable<{ count: number }> {
    return this.http.get<{ count: number }>(`${this.apiUrl}/count/pending`);
  }

  /**
   * Update report status (admin only)
   * PUT /api/reports/{id}/status
   */
  updateReportStatus(reportId: number, request: UpdateReportStatusRequest): Observable<ReportResponse> {
    return this.http.put<ReportResponse>(`${this.apiUrl}/${reportId}/status`, request);
  }

  /**
   * Get user-friendly label for report reason
   */
  getReasonLabel(reason: ReportReason): string {
    const labels: Record<ReportReason, string> = {
      [ReportReason.SPAM]: 'Spam or misleading content',
      [ReportReason.HARASSMENT]: 'Harassment or bullying',
      [ReportReason.HATE_SPEECH]: 'Hate speech or discrimination',
      [ReportReason.VIOLENCE]: 'Violence or dangerous content',
      [ReportReason.SEXUAL_CONTENT]: 'Sexual or inappropriate content',
      [ReportReason.FALSE_INFORMATION]: 'False information or misinformation',
      [ReportReason.COPYRIGHT]: 'Copyright or trademark violation',
      [ReportReason.OTHER]: 'Other reason'
    };
    return labels[reason];
  }

  /**
   * Get all available report reasons
   */
  getAllReasons(): { value: ReportReason; label: string }[] {
    return Object.values(ReportReason).map(reason => ({
      value: reason,
      label: this.getReasonLabel(reason)
    }));
  }
}
