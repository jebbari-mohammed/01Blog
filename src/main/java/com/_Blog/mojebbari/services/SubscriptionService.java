package com._Blog.mojebbari.services;

import com._Blog.mojebbari.dto.UserSummaryResponse;
import com._Blog.mojebbari.models.Subscription;
import com._Blog.mojebbari.models.User;
import com._Blog.mojebbari.repositories.SubscriptionRepository;
import com._Blog.mojebbari.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for managing user subscriptions (follow/unfollow)
 * 
 * @Transactional: All methods run in a database transaction
 * - If something fails, all changes are rolled back
 * - Important for data consistency
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    /**
     * Follow a user
     * 
     * Business Rules:
     * 1. Can't follow yourself
     * 2. Can't follow the same person twice
     * 3. Both users must exist
     * 
     * @param followerId - ID of the user who wants to follow
     * @param followingId - ID of the user to follow
     */
    public void followUser(Long followerId, Long followingId) {
        // Rule 1: Prevent self-follow
        if (followerId.equals(followingId)) {
            throw new IllegalArgumentException("You cannot follow yourself");
        }

        // Find both users
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new EntityNotFoundException("Follower not found"));
        
        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new EntityNotFoundException("User to follow not found"));

        // Rule 2: Check if already following
        if (subscriptionRepository.existsByFollowerAndFollowing(follower, following)) {
            throw new IllegalStateException("You are already following this user");
        }

        // Create subscription
        Subscription subscription = Subscription.builder()
                .follower(follower)
                .following(following)
                .build();

        subscriptionRepository.save(subscription);
        
        // TODO: Create a notification for the followed user (Stage 4)
    }

    /**
     * Unfollow a user
     * 
     * @param followerId - ID of the user who wants to unfollow
     * @param followingId - ID of the user to unfollow
     */
    public void unfollowUser(Long followerId, Long followingId) {
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new EntityNotFoundException("Follower not found"));
        
        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Find and delete the subscription
        Subscription subscription = subscriptionRepository.findByFollowerAndFollowing(follower, following)
                .orElseThrow(() -> new EntityNotFoundException("You are not following this user"));

        subscriptionRepository.delete(subscription);
    }

    /**
     * Get list of users that this user is following
     * 
     * @param userId - The user whose "following" list we want
     * @param currentUserId - The logged-in user (to check if they follow these users)
     * @return List of UserSummaryResponse
     */
    public List<UserSummaryResponse> getFollowing(Long userId, Long currentUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Get all subscriptions where this user is the follower
        List<Subscription> subscriptions = subscriptionRepository.findByFollower(user);

        // Get IDs of users that currentUser follows (for isFollowing field)
        Set<Long> currentUserFollowingIds = subscriptionRepository
                .findFollowingIdsByFollowerId(currentUserId)
                .stream()
                .collect(Collectors.toSet());

        // Convert to DTOs
        return subscriptions.stream()
                .map(subscription -> mapToUserSummary(
                        subscription.getFollowing(), 
                        currentUserFollowingIds.contains(subscription.getFollowing().getId())
                ))
                .collect(Collectors.toList());
    }

    /**
     * Get list of followers of this user
     * 
     * @param userId - The user whose followers we want
     * @param currentUserId - The logged-in user (to check if they follow these users)
     * @return List of UserSummaryResponse
     */
    public List<UserSummaryResponse> getFollowers(Long userId, Long currentUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Get all subscriptions where this user is being followed
        List<Subscription> subscriptions = subscriptionRepository.findByFollowing(user);

        // Get IDs of users that currentUser follows
        Set<Long> currentUserFollowingIds = subscriptionRepository
                .findFollowingIdsByFollowerId(currentUserId)
                .stream()
                .collect(Collectors.toSet());

        // Convert to DTOs
        return subscriptions.stream()
                .map(subscription -> mapToUserSummary(
                        subscription.getFollower(), 
                        currentUserFollowingIds.contains(subscription.getFollower().getId())
                ))
                .collect(Collectors.toList());
    }

    /**
     * Check if follower is following the user
     * 
     * @param followerId - The potential follower
     * @param followingId - The user being followed
     * @return true if following, false otherwise
     */
    public boolean isFollowing(Long followerId, Long followingId) {
        User follower = userRepository.findById(followerId).orElse(null);
        User following = userRepository.findById(followingId).orElse(null);
        
        if (follower == null || following == null) {
            return false;
        }
        
        return subscriptionRepository.existsByFollowerAndFollowing(follower, following);
    }

    /**
     * Get follower count for a user
     */
    public long getFollowerCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return subscriptionRepository.countByFollowing(user);
    }

    /**
     * Get following count for a user
     */
    public long getFollowingCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return subscriptionRepository.countByFollower(user);
    }

    /**
     * Helper method to convert User to UserSummaryResponse
     */
    private UserSummaryResponse mapToUserSummary(User user, boolean isFollowing) {
        return UserSummaryResponse.builder()
                .id(user.getId())
                .username(user.getUsername()) // Use actual username, not email
                .bio(user.getBio())
                .profilePicture(user.getProfilePicture())
                .isFollowing(isFollowing)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
