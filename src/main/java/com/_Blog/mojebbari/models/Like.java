package com._Blog.mojebbari.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Like Entity - Represents a user liking a post
 * 
 * Relationship:
 * - Many-to-One with User (many likes can belong to one user)
 * - Many-to-One with Post (many likes can belong to one post)
 * 
 * Example:
 * - Alice likes Bob's post #1
 * - Charlie likes Bob's post #1
 * - Alice likes Bob's post #2
 * 
 * Database will have:
 * | id | user_id | post_id | created_at |
 * |----|---------|---------|------------|
 * | 1  | Alice   | Post#1  | timestamp  |
 * | 2  | Charlie | Post#1  | timestamp  |
 * | 3  | Alice   | Post#2  | timestamp  |
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "likes",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "post_id"}))
// UniqueConstraint: A user can only like a post ONCE (prevents double-liking)
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user who liked the post
     * 
     * @ManyToOne: Many likes can belong to one user
     * Example: Alice can have many likes (for different posts)
     * 
     * FetchType.LAZY: Don't load the full User object unless needed
     * (Performance optimization)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The post that was liked
     * 
     * @ManyToOne: Many likes can belong to one post
     * Example: Bob's post can have many likes (from different users)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    /**
     * When the like was created
     * 
     * @CreationTimestamp: Hibernate automatically sets this when like is created
     * Useful for:
     * - Showing "Liked on..."
     * - Sorting by most recent likes
     * - Analytics (likes over time)
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
