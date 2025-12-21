package com._Blog.mojebbari.repositories;

import com._Blog.mojebbari.models.Comment;
import com._Blog.mojebbari.models.Post;
import com._Blog.mojebbari.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Comment entity
 * 
 * Provides methods to query comments from database
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * Get all comments for a specific post
     * 
     * Sorted by creation time (oldest first, like chronological conversation)
     * 
     * Generated SQL:
     * SELECT * FROM comments 
     * WHERE post_id = ? 
     * ORDER BY created_at ASC
     * 
     * Usage:
     * List<Comment> comments = commentRepository.findByPostOrderByCreatedAtAsc(bobsPost);
     * // Returns all comments on Bob's post, oldest to newest
     * 
     * Example result:
     * [
     *   {text: "First!", createdAt: 10:00},
     *   {text: "Nice post!", createdAt: 10:05},
     *   {text: "Love it!", createdAt: 10:10}
     * ]
     */
    List<Comment> findByPostOrderByCreatedAtAsc(Post post);

    /**
     * Alternative: Get comments newest first
     * 
     * Some apps show newest comments first
     * 
     * Generated SQL:
     * SELECT * FROM comments 
     * WHERE post_id = ? 
     * ORDER BY created_at DESC
     */
    List<Comment> findByPostOrderByCreatedAtDesc(Post post);

    /**
     * Get all comments by a specific user
     * 
     * Used for:
     * - User profile showing all their comments
     * - Admin viewing user's comment history
     * 
     * Generated SQL:
     * SELECT * FROM comments 
     * WHERE user_id = ? 
     * ORDER BY created_at DESC
     */
    List<Comment> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Count total comments on a post
     * 
     * Used for:
     * - Showing "42 comments" under a post
     * - Analytics
     * 
     * Generated SQL:
     * SELECT COUNT(*) FROM comments WHERE post_id = ?
     * 
     * Usage:
     * long commentCount = commentRepository.countByPost(bobsPost);
     * // Returns: 42
     */
    long countByPost(Post post);

    /**
     * Count how many comments a user has made
     * 
     * Used for:
     * - User profile stats
     * - Admin dashboard
     * 
     * Generated SQL:
     * SELECT COUNT(*) FROM comments WHERE user_id = ?
     */
    long countByUser(User user);

    /**
     * Delete all comments on a post
     * 
     * Used when:
     * - Post is deleted (cascade delete comments)
     * - Admin removes post
     * 
     * Generated SQL:
     * DELETE FROM comments WHERE post_id = ?
     * 
     * ⚠️ IMPORTANT: Must be @Transactional
     */
    void deleteByPost(Post post);

    /**
     * Optional: If you implement nested comments (replies)
     * 
     * Get all replies to a specific comment:
     * 
     * Uncomment if you add parent_id to Comment entity:
     */
    // List<Comment> findByParentOrderByCreatedAtAsc(Comment parent);
}
