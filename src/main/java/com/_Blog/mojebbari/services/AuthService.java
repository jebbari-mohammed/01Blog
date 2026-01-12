package com._Blog.mojebbari.services;

import com._Blog.mojebbari.config.JwtService;
import com._Blog.mojebbari.dto.AuthenticationResponse;
import com._Blog.mojebbari.dto.LoginRequest;
import com._Blog.mojebbari.dto.RegisterRequest;
import com._Blog.mojebbari.models.Role;
import com._Blog.mojebbari.models.User;
import com._Blog.mojebbari.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    // Register a new user
    public AuthenticationResponse register(RegisterRequest request) {
        // Step 1: Create new user with hashed password
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // Encrypt password
                .role(Role.USER) // Everyone starts as regular user
                .build();

        // Step 2: Save user to database
        userRepository.save(user);

        // Step 3: Create tokens for the user
        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Step 4: Return tokens
        return AuthenticationResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // Login existing user
    public AuthenticationResponse login(LoginRequest request) {
        // Step 1: Check if username/email and password are correct
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getIdentifier(), // Can be email or username
                        request.getPassword()
                )
        );

        // Step 2: Find user in database
        User user = userRepository.findByEmailOrUsername(request.getIdentifier(), request.getIdentifier())
                .orElseThrow();

        // Step 3: Create tokens
        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Step 4: Return tokens
        return AuthenticationResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
    
    // Get new access token using refresh token
    public AuthenticationResponse refreshToken(String refreshToken) {
        // Step 1: Get user email from refresh token
        String userEmail = jwtService.extractUsername(refreshToken);
        
        // Step 2: Find user
        User user = userRepository.findByEmailOrUsername(userEmail, userEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        // Step 3: Check if refresh token is valid
        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        
        // Step 4: Create new access token
        String newAccessToken = jwtService.generateToken(user);
        
        // Step 5: Return new access token with same refresh token
        return AuthenticationResponse.builder()
                .token(newAccessToken)
                .refreshToken(refreshToken)
                .build();
    }
}