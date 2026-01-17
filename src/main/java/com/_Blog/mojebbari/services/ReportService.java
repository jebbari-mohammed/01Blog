package com._Blog.mojebbari.services;

import com._Blog.mojebbari.dto.CreateReportRequest;
import com._Blog.mojebbari.dto.ReportResponse;
import com._Blog.mojebbari.dto.UpdateReportStatusRequest;
import com._Blog.mojebbari.models.*;
import com._Blog.mojebbari.repositories.CommentRepository;
import com._Blog.mojebbari.repositories.PostRepository;
import com._Blog.mojebbari.repositories.ReportRepository;
import com._Blog.mojebbari.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ReportService - Business logic for reports
 * 
 * Handles:
 * - Creating reports (users)
 * - Viewing all reports (admins)
 * - Updating report status (admins)
 * - Filtering reports by status
 * - Preventing duplicate reports
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    /**
     * Create a new report
     * 
     * Business rules:
     * - User cannot report their own content
     * - User cannot report the same content twice
     * - Content must exist
     * 
     * @param request - Report details
     * @param reporterId - ID of user creating the report
     * @return Created report response
     */
    @Transactional
    public ReportResponse createReport(CreateReportRequest request, Long reporterId) {
        log.debug("Creating report: type={}, contentId={}, reporterId={}", 
                  request.getReportType(), request.getContentId(), reporterId);

        // Get reporter
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new EntityNotFoundException("Reporter not found"));

        Report report = Report.builder()
                .reportType(request.getReportType())
                .reason(request.getReason())
                .description(request.getDescription())
                .status(ReportStatus.PENDING)
                .reporter(reporter)
                .build();

        // Set the content being reported
        if (request.getReportType() == ReportType.POST) {
            // Check if already reported
            if (reportRepository.existsByReporterIdAndPostId(reporterId, request.getContentId())) {
                throw new IllegalStateException("You have already reported this post");
            }

            Post post = postRepository.findById(request.getContentId())
                    .orElseThrow(() -> new EntityNotFoundException("Post not found"));

            // Cannot report own post
            if (post.getUser().getId().equals(reporterId)) {
                throw new IllegalStateException("You cannot report your own post");
            }

            report.setPost(post);

        } else if (request.getReportType() == ReportType.COMMENT) {
            // Check if already reported
            if (reportRepository.existsByReporterIdAndCommentId(reporterId, request.getContentId())) {
                throw new IllegalStateException("You have already reported this comment");
            }

            Comment comment = commentRepository.findById(request.getContentId())
                    .orElseThrow(() -> new EntityNotFoundException("Comment not found"));

            // Cannot report own comment
            if (comment.getUser().getId().equals(reporterId)) {
                throw new IllegalStateException("You cannot report your own comment");
            }

            report.setComment(comment);
            
        } else if (request.getReportType() == ReportType.USER) {
            // Check if already reported
            if (reportRepository.existsByReporterIdAndReportedUserId(reporterId, request.getContentId())) {
                throw new IllegalStateException("You have already reported this user");
            }

            User reportedUser = userRepository.findById(request.getContentId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            // Cannot report yourself
            if (reportedUser.getId().equals(reporterId)) {
                throw new IllegalStateException("You cannot report yourself");
            }

            report.setReportedUser(reportedUser);
        }

        Report savedReport = reportRepository.save(report);
        log.info("Report created: id={}, type={}, reason={}", 
                 savedReport.getId(), savedReport.getReportType(), savedReport.getReason());

        return mapToResponse(savedReport);
    }

    /**
     * Get all reports (admin only)
     * 
     * @return List of all reports ordered by newest first
     */
    @Transactional(readOnly = true)
    public List<ReportResponse> getAllReports() {
        List<Report> reports = reportRepository.findAllByOrderByCreatedAtDesc();
        return reports.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get reports by status (admin only)
     * 
     * @param status - Filter by status
     * @return Filtered list of reports
     */
    @Transactional(readOnly = true)
    public List<ReportResponse> getReportsByStatus(ReportStatus status) {
        List<Report> reports = reportRepository.findByStatusOrderByCreatedAtDesc(status);
        return reports.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get count of pending reports (for notification badge)
     * 
     * @return Number of pending reports
     */
    @Transactional(readOnly = true)
    public long getPendingReportsCount() {
        return reportRepository.countByStatus(ReportStatus.PENDING);
    }

    /**
     * Update report status (admin only)
     * 
     * @param reportId - ID of report to update
     * @param request - New status and optional notes
     * @param adminId - ID of admin updating the report
     * @return Updated report response
     */
    @Transactional
    public ReportResponse updateReportStatus(Long reportId, UpdateReportStatusRequest request, Long adminId) {
        log.debug("Updating report status: reportId={}, newStatus={}, adminId={}", 
                  reportId, request.getStatus(), adminId);

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("Report not found"));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("Admin not found"));

        // Update report
        report.setStatus(request.getStatus());
        report.setAdminNotes(request.getAdminNotes());
        report.setReviewedBy(admin);
        report.setReviewedAt(LocalDateTime.now());

        Report updatedReport = reportRepository.save(report);
        log.info("Report updated: id={}, status={}, reviewedBy={}", 
                 updatedReport.getId(), updatedReport.getStatus(), admin.getUsername());

        return mapToResponse(updatedReport);
    }

    /**
     * Map Report entity to ReportResponse DTO
     */
    private ReportResponse mapToResponse(Report report) {
        String contentPreview = "";
        String contentAuthor = "";
        Long contentId = null;

        if (report.getPost() != null) {
            Post post = report.getPost();
            contentId = post.getId();
            contentPreview = post.getContent().length() > 100 
                    ? post.getContent().substring(0, 100) + "..." 
                    : post.getContent();
            contentAuthor = post.getUser().getUsername();
        } else if (report.getComment() != null) {
            Comment comment = report.getComment();
            contentId = comment.getId();
            contentPreview = comment.getText().length() > 100 
                    ? comment.getText().substring(0, 100) + "..." 
                    : comment.getText();
            contentAuthor = comment.getUser().getUsername();
        } else if (report.getReportedUser() != null) {
            User reportedUser = report.getReportedUser();
            contentId = reportedUser.getId();
            contentPreview = "User: @" + reportedUser.getUsername();
            if (reportedUser.getBio() != null && !reportedUser.getBio().isEmpty()) {
                contentPreview += " - " + (reportedUser.getBio().length() > 80 
                        ? reportedUser.getBio().substring(0, 80) + "..." 
                        : reportedUser.getBio());
            }
            contentAuthor = reportedUser.getUsername();
        }

        return ReportResponse.builder()
                .id(report.getId())
                .reportType(report.getReportType())
                .reason(report.getReason())
                .description(report.getDescription())
                .status(report.getStatus())
                .reporterId(report.getReporter().getId())
                .reporterUsername(report.getReporter().getUsername())
                .contentId(contentId)
                .contentPreview(contentPreview)
                .contentAuthor(contentAuthor)
                .reviewedById(report.getReviewedBy() != null ? report.getReviewedBy().getId() : null)
                .reviewedByUsername(report.getReviewedBy() != null ? report.getReviewedBy().getUsername() : null)
                .adminNotes(report.getAdminNotes())
                .reviewedAt(report.getReviewedAt())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
