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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import jakarta.servlet.http.HttpServletResponse; 
import org.springframework.http.MediaType;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity // Allows us to use @PreAuthorize in our controllers
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider; // Our bean from ApplicationConfig

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                // 1. Enable CORS with our configuration
                http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

                // 2. Disable CSRF (we use stateless JWTs)
                http.csrf(csrf -> csrf.disable());

                // 3. Define the "white list" - endpoints that DON'T need authentication
                http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**") // All auth routes
                        .permitAll()
                        .requestMatchers("/uploads/**") // Allow access to uploaded files
                        .permitAll()
                        .anyRequest() // Any other request...
                        .authenticated() // ...must be authenticated
                );

                // 4. We use stateless sessions; Spring won't create sessions
                http.sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

                // 5. Tell Spring to use our custom beans
                http.authenticationProvider(authenticationProvider);
                http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
                
                // 6. Custom exception handling for authentication/authorization failures
                http.exceptionHandling(exception -> exception
                    // Handle 403 Forbidden (Authorization failure)
                    .accessDeniedHandler((request, response, accessDeniedException) -> {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        response.getWriter().write(
                            "{\"error\": \"Authorization Failed: " + accessDeniedException.getMessage() + "\"}"
                        );
                    })
                    // Handle 401 Unauthorized (Authentication failure, e.g., bad token)
                    .authenticationEntryPoint((request, response, authException) -> {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        response.getWriter().write("{\"error\": \"No Post Found\"}");
                    })
                );

        return http.build();
    }

    /**
     * CORS Configuration - Allows frontend (Angular) to communicate with backend
     * 
     * What it does:
     * - Allows requests from http://localhost:4200 (Angular dev server)
     * - Allows credentials (cookies, authorization headers)
     * - Allows all HTTP methods (GET, POST, PUT, DELETE, etc.)
     * - Allows all headers
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow Angular dev server (both default port and any alternative port)
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:4200",
            "http://localhost:64266"  // Added for current Angular server
        ));
        
        // Allow credentials (JWT tokens in Authorization header)
        configuration.setAllowCredentials(true);
        
        // Allow all HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // Allow all headers
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // Expose Authorization header to frontend
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        
        // Apply this configuration to all endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}