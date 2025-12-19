package com._Blog.mojebbari.repositories;

import com._Blog.mojebbari.models.Subscription;
import com._Blog.mojebbari.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Subscription entity
 * 
 * Spring Data JPA will automatically generate implementations for these methods
 * based on the method names (Query Methods) or @Query annotations
 */
@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    /**
     * Check if follower is already following the user
     * 
     * Example: isFollowing(Alice, Bob) → true if Alice follows Bob
     * 
     * Query Method Naming Convention:
     * - existsBy = Returns boolean (true/false)
     * - FollowerAndFollowing = WHERE follower = ? AND following = ?
     */
    boolean existsByFollowerAndFollowing(User follower, User following);

    /**
     * Find the subscription record between two users
     * 
     * Useful for unfollow: need to find and delete the subscription
     */
    Optional<Subscription> findByFollowerAndFollowing(User follower, User following);

    /**
     * Get all users that this user is following
     * 
     * Example: findByFollower(Alice) → [Bob, Charlie, Dave]
     * Returns subscriptions where Alice is the follower
     */
    List<Subscription> findByFollower(User follower);

    /**
     * Get all followers of this user
     * 
     * Example: findByFollowing(Bob) → [Alice, Charlie]
     * Returns subscriptions where Bob is being followed
     */
    List<Subscription> findByFollowing(User following);

    /**
     * Count how many users this user is following
     * 
     * Example: countByFollower(Alice) → 3 (Alice follows 3 people)
     */
    long countByFollower(User follower);

    /**
     * Count how many followers this user has
     * 
     * Example: countByFollowing(Bob) → 2 (Bob has 2 followers)
     */
    long countByFollowing(User following);

    /**
     * Get IDs of users that the follower is following
     * 
     * This is optimized - only fetches IDs, not full User objects
     * Useful for checking if current user follows someone
     * 
     * @Query annotation lets us write custom JPQL (JPA Query Language)
     */
    @Query("SELECT s.following.id FROM Subscription s WHERE s.follower.id = :followerId")
    List<Long> findFollowingIdsByFollowerId(@Param("followerId") Long followerId);

    /**
     * Delete subscription by follower and following
     * 
     * Used for unfollow functionality
     */
    void deleteByFollowerAndFollowing(User follower, User following);
}
