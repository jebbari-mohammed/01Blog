package com._Blog.mojebbari.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Report Entity - Represents a user report for inappropriate content
 * 
 * Users can report:
 * - Posts (inappropriate content, spam, harassment, etc.)
 * - Comments (offensive, spam, etc.)
 * 
 * Reports are reviewed by admins who can:
 * - Mark as REVIEWED (under investigation)
 * - Mark as RESOLVED (action taken)
 * - Mark as DISMISSED (no action needed)
 */
@Entity
@Table(name = "reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Type of content being reported
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportType reportType;

    /**
     * Reason for the report
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportReason reason;

    /**
     * Additional details provided by the reporter
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Current status of the report
     */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status = ReportStatus.PENDING;

    /**
     * User who created the report
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    /**
     * Post being reported (null if reporting a comment)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    /**
     * Comment being reported (null if reporting a post or user)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    /**
     * User being reported (null if reporting a post or comment)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_user_id")
    private User reportedUser;

    /**
     * Admin who reviewed the report
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_id")
    private User reviewedBy;

    /**
     * Admin notes about the resolution
     */
    @Column(columnDefinition = "TEXT")
    private String adminNotes;

    /**
     * When the report was created
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * When the report was reviewed/resolved
     */
    private LocalDateTime reviewedAt;

    /**
     * Validate that exactly one content type is set
     */
    @PrePersist
    @PreUpdate
    private void validateReport() {
        int contentCount = 0;
        if (post != null) contentCount++;
        if (comment != null) contentCount++;
        if (reportedUser != null) contentCount++;
        
        if (contentCount != 1) {
            throw new IllegalStateException("Report must have exactly one content type (post, comment, or user)");
        }
    }
}
