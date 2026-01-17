package com._Blog.mojebbari.models;

/**
 * ReportReason - Reason for reporting content
 */
public enum ReportReason {
    SPAM("Spam or misleading content"),
    HARASSMENT("Harassment or bullying"),
    HATE_SPEECH("Hate speech or discrimination"),
    VIOLENCE("Violence or dangerous content"),
    SEXUAL_CONTENT("Sexual or inappropriate content"),
    FALSE_INFORMATION("False information or misinformation"),
    COPYRIGHT("Copyright or trademark violation"),
    OTHER("Other reason");

    private final String description;

    ReportReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
