package com._Blog.mojebbari.controllers;

import com._Blog.mojebbari.dto.AuthenticationResponse;
import com._Blog.mojebbari.dto.LoginRequest;
import com._Blog.mojebbari.dto.RegisterRequest;
import com._Blog.mojebbari.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        // @Valid: Checks your DTO for @NotBlank, @Email, etc.
        // If valid, it runs the code below.
        // If invalid, Spring throws a 400 error automatically.
        return ResponseEntity.ok(authService.register(request));
    }

    /**
     * Register admin user - DISABLED FOR SECURITY
     * To enable: uncomment the endpoint below
     * In production, this should be secured or removed entirely
     */
    // @PostMapping("/register-admin")
    // public ResponseEntity<AuthenticationResponse> registerAdmin(
    //         @Valid @RequestBody RegisterRequest request
    // ) {
    //     return ResponseEntity.ok(authService.registerAdmin(request));
    // }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refreshToken(
            @RequestHeader("Authorization") String authHeader
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid Authorization header");
        }
        String refreshToken = authHeader.substring(7);
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }
}