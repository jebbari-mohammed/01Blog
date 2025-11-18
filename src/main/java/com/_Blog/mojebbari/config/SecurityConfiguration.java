package com._Blog.mojebbari.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity // Allows us to use @PreAuthorize in our controllers
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider; // Our bean from ApplicationConfig

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                // 1. Disable CSRF (we use stateless JWTs)
                http.csrf(csrf -> csrf.disable());

                // 2. Define the "white list" - endpoints that DON'T need authentication
                http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**") // All auth routes
                        .permitAll()
                        .anyRequest() // Any other request...
                        .authenticated() // ...must be authenticated
                );

                // 3. We use stateless sessions; Spring won't create sessions
                http.sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

                // 4. Tell Spring to use our custom beans
                http.authenticationProvider(authenticationProvider);
                http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}