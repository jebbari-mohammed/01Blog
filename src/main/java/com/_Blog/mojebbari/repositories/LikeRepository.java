package com._Blog.mojebbari.repositories;

import com._Blog.mojebbari.models.Like;
import com._Blog.mojebbari.models.Post;
import com._Blog.mojebbari.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Like entity
 * 
 * Spring Data JPA automatically implements these methods
 * based on method naming conventions
 */
@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    /**
     * Check if a user already liked a post
     * 
     * Method name breakdown:
     * - existsBy = Returns boolean (true/false)
     * - UserAndPost = WHERE user = ? AND post = ?
     * 
     * Generated SQL:
     * SELECT EXISTS(
     *   SELECT 1 FROM likes 
     *   WHERE user_id = ? AND post_id = ?
     * )
     * 
     * Usage:
     * boolean hasLiked = likeRepository.existsByUserAndPost(alice, bobsPost);
     * // Returns true if Alice already liked Bob's post
     */
    boolean existsByUserAndPost(User user, Post post);

    /**
     * Find the like record for a specific user and post
     * 
     * Used for:
     * - Unlike functionality (need to find the like to delete it)
     * - Getting details of when user liked the post
     * 
     * Returns Optional because:
     * - User might not have liked the post yet
     * - Safer than returning null
     * 
     * Generated SQL:
     * SELECT * FROM likes 
     * WHERE user_id = ? AND post_id = ?
     */
    Optional<Like> findByUserAndPost(User user, Post post);

    /**
     * Get all likes for a specific post
     * 
     * Used for:
     * - Showing list of users who liked a post
     * - "Liked by Alice, Bob, and 10 others"
     * 
     * Generated SQL:
     * SELECT * FROM likes WHERE post_id = ?
     * ORDER BY created_at DESC (most recent first)
     */
    List<Like> findByPostOrderByCreatedAtDesc(Post post);

    /**
     * Get all posts liked by a specific user
     * 
     * Used for:
     * - User's profile showing all posts they liked
     * - "Alice's liked posts" page
     * 
     * Generated SQL:
     * SELECT * FROM likes WHERE user_id = ?
     * ORDER BY created_at DESC
     */
    List<Like> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Count how many likes a post has
     * 
     * Method name breakdown:
     * - countBy = Returns long (number count)
     * - Post = WHERE post = ?
     * 
     * Generated SQL:
     * SELECT COUNT(*) FROM likes WHERE post_id = ?
     * 
     * Usage:
     * long likeCount = likeRepository.countByPost(bobsPost);
     * // Returns: 42 (Bob's post has 42 likes)
     */
    long countByPost(Post post);

    /**
     * Count how many posts a user has liked
     * 
     * Generated SQL:
     * SELECT COUNT(*) FROM likes WHERE user_id = ?
     * 
     * Usage:
     * long likedCount = likeRepository.countByUser(alice);
     * // Returns: 15 (Alice has liked 15 posts)
     */
    long countByUser(User user);

    /**
     * Delete a like by user and post
     * 
     * Used for: Unlike functionality
     * 
     * Generated SQL:
     * DELETE FROM likes WHERE user_id = ? AND post_id = ?
     * 
     * ⚠️ IMPORTANT: Must be called within @Transactional method!
     * 
     * Usage:
     * likeRepository.deleteByUserAndPost(alice, bobsPost);
     * // Removes Alice's like from Bob's post
     */
    void deleteByUserAndPost(User user, Post post);

    /**
     * Delete all likes for a specific post
     * 
     * Used when:
     * - Post is deleted (cascade delete all likes)
     * - Admin removes post
     * 
     * Generated SQL:
     * DELETE FROM likes WHERE post_id = ?
     * 
     * ⚠️ Must be @Transactional
     */
    void deleteByPost(Post post);
}
