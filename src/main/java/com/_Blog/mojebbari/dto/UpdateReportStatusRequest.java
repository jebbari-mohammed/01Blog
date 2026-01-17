package com._Blog.mojebbari.dto;

import com._Blog.mojebbari.models.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UpdateReportStatusRequest - DTO for admin to update report status
 * 
 * Admin can:
 * - Change status (PENDING -> REVIEWED -> RESOLVED/DISMISSED)
 * - Add notes about the action taken
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateReportStatusRequest {
    
    private ReportStatus status;
    private String adminNotes; // Optional notes about the resolution
}
