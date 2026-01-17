package com._Blog.mojebbari.models;

/**
 * ReportStatus - Current status of a report
 */
public enum ReportStatus {
    PENDING,     // Report submitted, awaiting review
    REVIEWED,    // Admin is investigating
    RESOLVED,    // Action taken (content removed, user warned, etc.)
    DISMISSED    // No action needed (false report, not violating rules)
}
