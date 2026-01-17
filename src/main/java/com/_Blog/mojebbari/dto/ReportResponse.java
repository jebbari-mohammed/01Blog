package com._Blog.mojebbari.dto;

import com._Blog.mojebbari.models.ReportReason;
import com._Blog.mojebbari.models.ReportStatus;
import com._Blog.mojebbari.models.ReportType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ReportResponse - DTO for returning report information
 * 
 * Contains:
 * - Report details (reason, status, description)
 * - Reporter information
 * - Content information (post/comment details)
 * - Review information (if reviewed)
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportResponse {
    
    private Long id;
    private ReportType reportType;
    private ReportReason reason;
    private String description;
    private ReportStatus status;
    
    // Reporter info
    private Long reporterId;
    private String reporterUsername;
    
    // Content info
    private Long contentId;
    private String contentPreview; // First 100 chars of post/comment
    private String contentAuthor;
    
    // Review info
    private Long reviewedById;
    private String reviewedByUsername;
    private String adminNotes;
    private LocalDateTime reviewedAt;
    
    // Timestamps
    private LocalDateTime createdAt;
}
