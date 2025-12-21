package com._Blog.mojebbari.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Comment Entity - Represents a comment on a post
 * 
 * Relationship:
 * - Many-to-One with User (many comments can belong to one user)
 * - Many-to-One with Post (many comments can belong to one post)
 * 
 * Example:
 * - Alice comments "Great post!" on Bob's photo
 * - Charlie comments "Love it!" on Bob's photo
 * 
 * Database will have:
 * | id | user_id | post_id | text         | created_at | updated_at |
 * |----|---------|---------|--------------|------------|------------|
 * | 1  | Alice   | Post#1  | "Great!"     | timestamp  | timestamp  |
 * | 2  | Charlie | Post#1  | "Love it!"   | timestamp  | timestamp  |
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The comment text content
     * 
     * @Column(columnDefinition = "TEXT"):
     * - TEXT type allows longer comments (up to ~65,000 characters)
     * - VARCHAR has limits (usually 255 or 1000 chars)
     * - nullable = false: Comment must have content
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;

    /**
     * The user who wrote the comment
     * 
     * @ManyToOne: Many comments can belong to one user
     * Example: Alice can write many comments on different posts
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The post that was commented on
     * 
     * @ManyToOne: Many comments can belong to one post
     * Example: Bob's post can have many comments from different users
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    /**
     * When the comment was created
     * 
     * @CreationTimestamp: Automatically set when comment is first saved
     * updatable = false: This value never changes (immutable)
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * When the comment was last updated
     * 
     * @UpdateTimestamp: Automatically updates every time comment is modified
     * 
     * Use case:
     * - User posts comment at 10:00 AM → createdAt = 10:00, updatedAt = 10:00
     * - User edits comment at 11:00 AM → createdAt = 10:00, updatedAt = 11:00
     * - UI can show "Edited" badge if updatedAt > createdAt
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Optional: For nested comments (replies to comments)
     * 
     * If you want Twitter-style threaded replies:
     * - Comment can have a parent comment
     * - null = top-level comment
     * - non-null = reply to another comment
     * 
     * Example:
     * Comment 1: "Great post!" (parent = null)
     *   └─ Comment 2: "I agree!" (parent = Comment 1)
     * 
     * Uncomment if you want this feature:
     */
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "parent_id")
    // private Comment parent;
}
