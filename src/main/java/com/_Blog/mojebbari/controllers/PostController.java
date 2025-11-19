package com._Blog.mojebbari.controllers;

import com._Blog.mojebbari.dto.CreatePostRequest;
import com._Blog.mojebbari.dto.PostResponse;
import com._Blog.mojebbari.services.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // POST /api/posts - Create a new post
    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @Valid @RequestBody CreatePostRequest request,
            Principal principal // Spring automatically injects the logged-in user here!
    ) {
        // principal.getName() will return the email/username from the JWT
        return ResponseEntity.ok(postService.createPost(request, principal.getName()));
    }

    // GET /api/posts - Get all posts (Feed)
    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    // GET /api/posts/user/{username} - Get user's posts
    @GetMapping("/user/{username}")
    public ResponseEntity<List<PostResponse>> getUserPosts(@PathVariable String username) {
        return ResponseEntity.ok(postService.getPostsByUsername(username));
    }
    @DeleteMapping("/{postId}") // The {postId} is passed in the URL path
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId, // Get the ID from the URL path
            Principal principal // Get the identity of the logged-in user
    ) {
        postService.deletePost(postId, principal.getName());
        return ResponseEntity.noContent().build(); // 204 No Content for successful deletion
    }
}
