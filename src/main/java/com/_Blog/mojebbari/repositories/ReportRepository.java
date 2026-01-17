package com._Blog.mojebbari.repositories;

import com._Blog.mojebbari.models.Report;
import com._Blog.mojebbari.models.ReportStatus;
import com._Blog.mojebbari.models.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ReportRepository - Data access for Report entity
 * 
 * Provides methods to:
 * - Find reports by status
 * - Find reports by type (POST/COMMENT)
 * - Get all reports ordered by creation date
 * - Check if user already reported specific content
 */
@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    /**
     * Find all reports with a specific status
     * Ordered by creation date (newest first)
     */
    List<Report> findByStatusOrderByCreatedAtDesc(ReportStatus status);

    /**
     * Find all reports by type
     */
    List<Report> findByReportTypeOrderByCreatedAtDesc(ReportType reportType);

    /**
     * Find all reports ordered by creation date
     */
    List<Report> findAllByOrderByCreatedAtDesc();

    /**
     * Check if a user already reported a specific post
     * Prevents duplicate reports
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
           "FROM Report r WHERE r.reporter.id = :reporterId AND r.post.id = :postId")
    boolean existsByReporterIdAndPostId(Long reporterId, Long postId);

    /**
     * Check if a user already reported a specific comment
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
           "FROM Report r WHERE r.reporter.id = :reporterId AND r.comment.id = :commentId")
    boolean existsByReporterIdAndCommentId(Long reporterId, Long commentId);

    /**
     * Check if a user already reported another specific user
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
           "FROM Report r WHERE r.reporter.id = :reporterId AND r.reportedUser.id = :reportedUserId")
    boolean existsByReporterIdAndReportedUserId(Long reporterId, Long reportedUserId);

    /**
     * Find all reports for a specific post
     * Useful for seeing how many times a post was reported
     */
    List<Report> findByPostIdOrderByCreatedAtDesc(Long postId);

    /**
     * Find all reports for a specific comment
     */
    List<Report> findByCommentIdOrderByCreatedAtDesc(Long commentId);

    /**
     * Find all reports for a specific user
     */
    List<Report> findByReportedUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Count pending reports
     * Useful for showing notification badge to admins
     */
    long countByStatus(ReportStatus status);
}
