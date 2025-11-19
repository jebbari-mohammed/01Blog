package com._Blog.mojebbari.services;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import com._Blog.mojebbari.dto.CreatePostRequest;
import com._Blog.mojebbari.dto.PostResponse;
import com._Blog.mojebbari.models.Post;
import com._Blog.mojebbari.models.Role;
import com._Blog.mojebbari.models.User;
import com._Blog.mojebbari.repositories.PostRepository;
import com._Blog.mojebbari.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 1. Create a Post
    public PostResponse createPost(CreatePostRequest request, String username) {
        // Find the user who is making the request
        User user = userRepository.findByEmailOrUsername(username, username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Build the post
        Post post = Post.builder()
                .text(request.getText())
                .mediaUrl(request.getMediaUrl())
                .user(user) // LINK THE POST TO THE USER
                .build();

        // Save to DB
        Post savedPost = postRepository.save(post);

        // Convert to DTO to return
        return mapToResponse(savedPost);
    }

    // 2. Get All Posts (For the feed)
    public List<PostResponse> getAllPosts() {
        return postRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // 3. Get Posts by Specific User (For profile page)
    public List<PostResponse> getPostsByUsername(String username) {
        User user = userRepository.findByEmailOrUsername(username, username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return postRepository.findAllByUser(user).stream() // Using the method we created earlier!
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    public void deletePost(Long postId, String principalUsername) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with ID: " + postId));

        // Find the User who is trying to delete the post
        User principalUser = userRepository.findByEmailOrUsername(principalUsername, principalUsername)
                .orElseThrow(() -> new EntityNotFoundException("Logged-in user not found"));

        // AUTHORIZATION LOGIC: 
        // 1. Check if the current user is the author of the post.
        boolean isAuthor = post.getUser().getEmail().equals(principalUsername) || 
                           post.getUser().getUsername().equals(principalUsername);
        
        // 2. Check if the current user is an Admin.
        boolean isAdmin = principalUser.getRole() == Role.ADMIN;
        
        // If they are neither the author NOR an admin, deny access.
        if (!isAuthor && !isAdmin) {
             throw new AccessDeniedException("You do not have permission to delete this post.");
        }
        
        // If the check passes (author or admin), delete the post
        postRepository.delete(post);
    }

    // Helper method to convert Post Entity -> PostResponse DTO
    private PostResponse mapToResponse(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .text(post.getText())
                .mediaUrl(post.getMediaUrl())
                .timestamp(post.getTimestamp())
                .authorUsername(post.getUser().getUsername()) // Grab just the username
                .build();
    }
}