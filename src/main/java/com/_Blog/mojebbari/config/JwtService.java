package com._Blog.mojebbari.config;

import com._Blog.mojebbari.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret-key}")
    private String secretKey;
    
    @Value("${jwt.expiration.hours}")
    private long expirationHours;
    
    @Value("${jwt.refresh-expiration.days}")
    private long refreshTokenExpirationDays;

    // 2. had l method bach njebdo username mn token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token); // extractAllClaims katjib lina kolchi mn token
        return claimsResolver.apply(claims); // hna kan applyiw function 3la claims bach njibo li bghina
    }

    // 3. Methods to generate a new token
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();
        
        // Add custom claims if userDetails is our User entity
        if (userDetails instanceof User) {
            User user = (User) userDetails;
            extraClaims.put("userId", user.getId());
            extraClaims.put("username", user.getUsername());
            extraClaims.put("email", user.getEmail()); // Add email to token
            extraClaims.put("role", user.getRole().name());
        }
        
        return generateToken(extraClaims, userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        long expirationMs = expirationHours * 60 * 60 * 1000; // Convert hours to milliseconds
        return buildToken(extraClaims, userDetails, expirationMs);
    }
    
    // NEW: Generate refresh token
    public String generateRefreshToken(UserDetails userDetails) {
        long expirationMs = refreshTokenExpirationDays * 24 * 60 * 60 * 1000; // Convert days to milliseconds
        return buildToken(new HashMap<>(), userDetails, expirationMs);
    }
    
    // Helper method to build tokens
    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expirationMs) {
        return Jwts.builder()
                .setClaims(extraClaims)  // hna kanzido ay claims bghina
                .setSubject(userDetails.getUsername()) // user's email
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 4. Methods to validate a token
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // 5. Helper methods (private)
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}


