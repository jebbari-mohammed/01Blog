package com._Blog.mojebbari.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Subscription Entity - Represents the "follow" relationship between users
 * 
 * This creates a Many-to-Many relationship between users:
 * - One user (follower) can follow MANY users
 * - One user can be followed by MANY users (followers)
 * 
 * Example:
 * If Alice follows Bob:
 *   - follower = Alice
 *   - following = Bob
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "subscriptions",
       uniqueConstraints = @UniqueConstraint(columnNames = {"follower_id", "following_id"}))
// UniqueConstraint ensures: Alice can't follow Bob twice
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user who is FOLLOWING someone
     * Example: If Alice follows Bob, follower = Alice
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;

    /**
     * The user being FOLLOWED
     * Example: If Alice follows Bob, following = Bob
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id", nullable = false)
    private User following;

    /**
     * When the subscription was created
     * Useful for "Followed since..." display
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
