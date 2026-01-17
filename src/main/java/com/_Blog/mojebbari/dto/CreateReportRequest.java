package com._Blog.mojebbari.dto;

import com._Blog.mojebbari.models.ReportReason;
import com._Blog.mojebbari.models.ReportType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CreateReportRequest - DTO for creating a new report
 * 
 * User submits:
 * - Type (POST or COMMENT)
 * - ID of the content being reported
 * - Reason for the report
 * - Optional description with more details
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateReportRequest {
    
    private ReportType reportType;
    private Long contentId; // Post ID or Comment ID
    private ReportReason reason;
    private String description; // Optional additional details
}
