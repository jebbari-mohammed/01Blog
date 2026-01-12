package com._Blog.mojebbari.services;

import com._Blog.mojebbari.dto.UpdateProfileRequest;
import com._Blog.mojebbari.dto.UserProfileResponse;
import com._Blog.mojebbari.models.User;
import com._Blog.mojebbari.repositories.PostRepository;
import com._Blog.mojebbari.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service for user profile management
 * 
 * Handles:
 * - Getting user profiles
 * - Updating user profiles
 * - Profile statistics (follower count, post count, etc.)
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final SubscriptionService subscriptionService;

    /**
     * Get a user's public profile
     * 
     * @param username - Username of the profile to view
     * @param currentUserId - ID of the logged-in user (to check if following)
     * @return UserProfileResponse with all profile info
     */
    public UserProfileResponse getUserProfile(String username, Long currentUserId) {
        // Find user by username
        User user = userRepository.findByEmailOrUsername(username, username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));

        // Get statistics
        long followerCount = subscriptionService.getFollowerCount(user.getId());
        long followingCount = subscriptionService.getFollowingCount(user.getId());
        int postCount = postRepository.findAllByUser(user).size();

        // Check if current user is following this profile
        boolean isFollowing = currentUserId != null && 
                              subscriptionService.isFollowing(currentUserId, user.getId());

        // Check if this is the current user's own profile
        boolean isOwnProfile = currentUserId != null && currentUserId.equals(user.getId());

        // Build response
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername()) // Use actual username, not email
                .email(user.getEmail()) // Consider privacy: maybe only show if isOwnProfile
                .bio(user.getBio())
                .profilePicture(user.getProfilePicture())
                .coverImage(user.getCoverImage())
                .createdAt(user.getCreatedAt())
                .followersCount(followerCount)
                .followingCount(followingCount)
                .postsCount(postCount)
                .isFollowing(isFollowing)
                .isOwnProfile(isOwnProfile)
                .build();
    }

    /**
     * Update user's own profile
     * 
     * Only updates non-null fields from the request
     * User can only update their own profile
     * 
     * @param userId - ID of the user updating their profile
     * @param request - Fields to update
     * @return Updated UserProfileResponse
     */
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Update only non-null fields
        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            // Check if username is already taken by another user
            userRepository.findByEmailOrUsername(request.getUsername(), request.getUsername())
                    .ifPresent(existingUser -> {
                        if (!existingUser.getId().equals(userId)) {
                            throw new IllegalArgumentException("Username already taken");
                        }
                    });
            user.setUsername(request.getUsername());
        }

        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }

        if (request.getProfilePicture() != null) {
            user.setProfilePicture(request.getProfilePicture());
        }

        if (request.getCoverImage() != null) {
            user.setCoverImage(request.getCoverImage());
        }

        // Save updated user
        User updatedUser = userRepository.save(user);

        // Return updated profile
        return getUserProfile(updatedUser.getUsername(), userId);
    }
}
