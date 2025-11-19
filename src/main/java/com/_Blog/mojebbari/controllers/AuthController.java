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

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }
}