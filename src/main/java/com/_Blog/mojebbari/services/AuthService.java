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

    // --- REGISTER ---
    public AuthenticationResponse register(RegisterRequest request) {
        // 1. Create the user object from the request
        var user = User.builder()
                .username(request.getUsername()) // In your case, this might be a display name
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // Hash the password!
                .role(Role.USER) // Default role is always USER
                .build();

        // 2. Save to database
        userRepository.save(user);

        // 3. Generate tokens for them immediately
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        // 4. Return both tokens
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    // --- LOGIN ---
public AuthenticationResponse login(LoginRequest request) {
        // 1. Try to authenticate
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getIdentifier(), // Use the generic identifier
                        request.getPassword()
                )
        );

        // 2. Find the user in the DB (check both columns)
        var user = userRepository.findByEmailOrUsername(request.getIdentifier(), request.getIdentifier())
                .orElseThrow();

        // 3. Generate tokens (The token will still contain the user's Email as the subject, 
        //    because user.getUsername() in your User model returns the email. This is good!)
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }
    
    // --- REFRESH TOKEN ---
    public AuthenticationResponse refreshToken(String refreshToken) {
        String username = jwtService.extractUsername(refreshToken);
        var user = userRepository.findByEmailOrUsername(username, username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        
        var newAccessToken = jwtService.generateToken(user);
        
        return AuthenticationResponse.builder()
                .token(newAccessToken)
                .refreshToken(refreshToken) // Return same refresh token
                .build();
    }
}