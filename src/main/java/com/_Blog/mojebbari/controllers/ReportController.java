package com._Blog.mojebbari.controllers;

import com._Blog.mojebbari.dto.CreateReportRequest;
import com._Blog.mojebbari.dto.ReportResponse;
import com._Blog.mojebbari.dto.UpdateReportStatusRequest;
import com._Blog.mojebbari.models.ReportStatus;
import com._Blog.mojebbari.models.User;
import com._Blog.mojebbari.services.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ReportController - REST API for reports
 * 
 * Endpoints:
 * - POST /api/reports - Create a report (any authenticated user)
 * - GET /api/reports - Get all reports (admin only)
 * - GET /api/reports/pending - Get pending reports (admin only)
 * - GET /api/reports/count/pending - Get count of pending reports (admin only)
 * - PUT /api/reports/{id}/status - Update report status (admin only)
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportService reportService;

    /**
     * Create a new report
     * POST /api/reports
     * 
     * Any authenticated user can report content
     * 
     * @param request - Report details
     * @param principal - Current user
     * @return 201 Created with report response
     */
    @PostMapping
    public ResponseEntity<ReportResponse> createReport(
            @RequestBody CreateReportRequest request,
            @AuthenticationPrincipal User user) {
        
        log.info("POST /api/reports - User {} reporting {} ID {}", 
                 user.getUsername(), request.getReportType(), request.getContentId());

        ReportResponse report = reportService.createReport(request, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(report);
    }

    /**
     * Get all reports
     * GET /api/reports
     * 
     * Admin only
     * 
     * @return List of all reports
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReportResponse>> getAllReports() {
        log.info("GET /api/reports - Fetching all reports");
        List<ReportResponse> reports = reportService.getAllReports();
        return ResponseEntity.ok(reports);
    }

    /**
     * Get reports by status
     * GET /api/reports/status/{status}
     * 
     * Admin only
     * 
     * @param status - Filter by status (PENDING, REVIEWED, RESOLVED, DISMISSED)
     * @return Filtered list of reports
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReportResponse>> getReportsByStatus(@PathVariable ReportStatus status) {
        log.info("GET /api/reports/status/{} - Fetching reports by status", status);
        List<ReportResponse> reports = reportService.getReportsByStatus(status);
        return ResponseEntity.ok(reports);
    }

    /**
     * Get pending reports
     * GET /api/reports/pending
     * 
     * Admin only - convenience endpoint for most common filter
     * 
     * @return List of pending reports
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReportResponse>> getPendingReports() {
        log.info("GET /api/reports/pending - Fetching pending reports");
        List<ReportResponse> reports = reportService.getReportsByStatus(ReportStatus.PENDING);
        return ResponseEntity.ok(reports);
    }

    /**
     * Get count of pending reports
     * GET /api/reports/count/pending
     * 
     * Admin only - useful for notification badge
     * 
     * @return Count of pending reports
     */
    @GetMapping("/count/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> getPendingReportsCount() {
        log.info("GET /api/reports/count/pending - Fetching pending reports count");
        long count = reportService.getPendingReportsCount();
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * Update report status
     * PUT /api/reports/{id}/status
     * 
     * Admin only
     * 
     * @param id - Report ID
     * @param request - New status and optional admin notes
     * @param user - Current admin user
     * @return Updated report
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReportResponse> updateReportStatus(
            @PathVariable Long id,
            @RequestBody UpdateReportStatusRequest request,
            @AuthenticationPrincipal User user) {
        
        log.info("PUT /api/reports/{}/status - Admin {} updating status to {}", 
                 id, user.getUsername(), request.getStatus());

        try {
            ReportResponse report = reportService.updateReportStatus(id, request, user.getId());
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            log.error("Error updating report status: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
