# Backend Configuration Files

## 📖 What is Configuration?

**Configuration** classes define how your Spring Boot application works:
- **Security**: Who can access what?
- **Authentication**: How do users log in?
- **CORS**: Can frontend call backend from different port?
- **Beans**: What objects does Spring manage?
- **Exception Handling**: How to respond to errors?

Think of config as the **wiring diagram** for your application.

---

## 🔑 Key Concepts

### 1. @Configuration
Tells Spring: "This class contains bean definitions"
```java
@Configuration
public class MyConfig {
    @Bean
    public MyService myService() {
        return new MyService();
    }
}
```

### 2. @Bean
A bean is an object managed by Spring. Spring creates it, injects it where needed, and manages its lifecycle.
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();  // Spring will manage this
}
```

### 3. Dependency Injection
Spring automatically provides beans where needed:
```java
@Service
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    
    // Spring sees this constructor and injects passwordEncoder bean
    public AuthService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
}
```

---

## 📁 Configuration Files in this Project

### 1. SecurityConfig.java

**Purpose**: Configure Spring Security - authentication and authorization

**Key Components**:

#### A. Security Filter Chain
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        .csrf(csrf -> csrf.disable())  // Disable CSRF for REST API
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll()      // Login/register public
            .requestMatchers("/api/admin/**").hasRole("ADMIN") // Admin only
            .anyRequest().authenticated()                      // All else needs auth
        )
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // No sessions, use JWT
        )
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
}
```

**What This Does**:

1. **CSRF Disabled**
   - CSRF (Cross-Site Request Forgery) protection not needed for JWT-based APIs
   - JWT tokens provide security instead

2. **Public Endpoints**
   ```java
   .requestMatchers("/api/auth/**").permitAll()
   ```
   - `/api/auth/login` - Anyone can login
   - `/api/auth/register` - Anyone can register
   - No authentication needed

3. **Admin-Only Endpoints**
   ```java
   .requestMatchers("/api/admin/**").hasRole("ADMIN")
   ```
   - Only users with ADMIN role can access
   - Example: `/api/admin/reports` (view reports dashboard)

4. **Protected Endpoints**
   ```java
   .anyRequest().authenticated()
   ```
   - Everything else requires authentication
   - Must have valid JWT token

5. **Stateless Sessions**
   ```java
   .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
   ```
   - No server-side sessions
   - Each request must include JWT token
   - Server doesn't remember previous requests

6. **JWT Filter**
   ```java
   .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
   ```
   - Runs before Spring's default authentication filter
   - Extracts JWT token from Authorization header
   - Validates token and sets authentication

#### B. Password Encoder Bean
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

**What This Does**:
- Provides BCrypt password hasher throughout app
- BCrypt is one-way: `password123` → `$2a$10$N9qo8uLO...` (cannot reverse)
- Used in:
  - Registration: Hash password before saving
  - Login: Compare entered password with stored hash

**Example Usage**:
```java
// In AuthService
String hashedPassword = passwordEncoder.encode("password123");
// Result: $2a$10$N9qo8uLOLxYi.FmVsK2dte5VGJ7IvGO8H4z8xsJx5...

boolean matches = passwordEncoder.matches("password123", hashedPassword);
// Result: true
```

#### C. CORS Configuration
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));  // Angular frontend
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);  // Apply to all endpoints
    return source;
}
```

**What is CORS?**
- **Problem**: Browser security blocks requests from `http://localhost:4200` (Angular) to `http://localhost:8080` (Spring Boot) - different ports = different origins
- **Solution**: Server tells browser "It's OK, I allow requests from localhost:4200"

**Configuration Explained**:
- `setAllowedOrigins(["http://localhost:4200"])` - Frontend URL allowed
- `setAllowedMethods([...])` - HTTP methods allowed
- `setAllowedHeaders(["*"])` - All headers allowed (including Authorization)
- `setAllowCredentials(true)` - Allow cookies and auth headers

**Without CORS**: Browser shows error "has been blocked by CORS policy"
**With CORS**: Requests work!

---

### 2. JwtService.java

**Purpose**: Generate and validate JWT tokens

**What is JWT?**
- **JWT (JSON Web Token)**: Encrypted string containing user info
- Format: `header.payload.signature`
- Example: `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huIiwicm9sZSI6IlVTRVIifQ.signature`

**Decoded JWT**:
```json
{
  "sub": "john",           // Subject (username)
  "role": "USER",          // User role
  "iat": 1234567890,       // Issued at (timestamp)
  "exp": 1234603890        // Expiration (timestamp)
}
```

**Key Methods**:

#### A. Generate Token
```java
public String generateToken(UserDetails userDetails) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", userDetails.getAuthorities());
    
    return Jwts.builder()
        .setClaims(claims)
        .setSubject(userDetails.getUsername())
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))  // 10 hours
        .signWith(getSignInKey(), SignatureAlgorithm.HS256)
        .compact();
}
```

**What This Does**:
1. Creates claims (data to store in token)
2. Sets subject (username)
3. Sets issued time (now)
4. Sets expiration (10 hours from now)
5. Signs with secret key (prevents tampering)
6. Returns token string

**Why 10 hours?**
- Access tokens should be short-lived
- If stolen, attacker only has 10 hours
- User can get new token with refresh token

#### B. Generate Refresh Token
```java
public String generateRefreshToken(UserDetails userDetails) {
    return Jwts.builder()
        .setSubject(userDetails.getUsername())
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7))  // 7 days
        .signWith(getSignInKey(), SignatureAlgorithm.HS256)
        .compact();
}
```

**What is Refresh Token?**
- Longer-lived token (7 days)
- Used to get new access token when expired
- Stored securely in frontend

**Flow**:
1. User logs in → Get access token (10h) + refresh token (7d)
2. After 10 hours, access token expires
3. Frontend calls `/api/auth/refresh` with refresh token
4. Backend returns new access token
5. User stays logged in without re-entering password

#### C. Extract Username
```java
public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
}

public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
}

private Claims extractAllClaims(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(getSignInKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
}
```

**What This Does**:
- Decodes JWT token
- Extracts username from "sub" claim
- Used to identify which user made the request

#### D. Validate Token
```java
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
```

**What This Does**:
1. Extracts username from token
2. Checks username matches logged-in user
3. Checks token hasn't expired

**Returns**:
- `true` - Token valid, allow request
- `false` - Token invalid/expired, deny request

#### E. Secret Key
```java
@Value("${jwt.secret}")
private String SECRET_KEY;

private Key getSignInKey() {
    byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
    return Keys.hmacShaKeyFor(keyBytes);
}
```

**What This Does**:
- Reads secret key from `application.properties`
- Converts to cryptographic key
- Used to sign and verify tokens

**Security**:
- Secret key must be kept secret!
- Anyone with secret key can create valid tokens
- Use strong random key in production

---

### 3. ApplicationConfig.java

**Purpose**: Configure core application beans

**Key Beans**:

#### A. UserDetailsService
```java
@Bean
public UserDetailsService userDetailsService() {
    return username -> userRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
}
```

**What This Does**:
- Spring Security needs a way to load user from database
- This bean provides that functionality
- Lambda function: `username → User from database`

**Used By**:
- AuthenticationManager when validating login
- JwtAuthFilter when loading user from token

**Flow**:
1. User sends JWT token
2. JwtService extracts username from token
3. UserDetailsService loads full User from database
4. Spring Security checks if user exists and has correct role

#### B. AuthenticationProvider
```java
@Bean
public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService());
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
}
```

**What This Does**:
- Configures how Spring Security authenticates users
- Uses UserDetailsService to load user
- Uses PasswordEncoder to check password

**Authentication Flow**:
1. User sends username + password
2. AuthenticationProvider loads user via UserDetailsService
3. Compares password: `passwordEncoder.matches(enteredPassword, storedHash)`
4. If match → Authentication successful
5. If no match → Throws BadCredentialsException

#### C. AuthenticationManager
```java
@Bean
public AuthenticationManager authenticationManager(AuthenticationConfiguration config) 
    throws Exception {
    return config.getAuthenticationManager();
}
```

**What This Does**:
- Main entry point for authentication
- Uses AuthenticationProvider to do actual work
- Exposed as bean so AuthService can use it

**Used In**:
```java
// In AuthService.login()
Authentication authentication = authenticationManager.authenticate(
    new UsernamePasswordAuthenticationToken(username, password)
);
```

---

### 4. JwtAuthFilter.java

**Purpose**: Intercept every request and validate JWT token

**What is a Filter?**
- Runs before request reaches controller
- Can inspect request, modify it, or reject it
- Used for authentication, logging, etc.

**Flow Diagram**:
```
Request → JwtAuthFilter → SecurityFilterChain → Controller
   |           |               |                    |
   |        Extract JWT    Check roles          Handle request
   |        Validate       Allow/Deny
   |        Set auth
```

**Key Method**:
```java
@Override
protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
) throws ServletException, IOException {
    final String authHeader = request.getHeader("Authorization");
    
    // 1. Check if Authorization header exists
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        filterChain.doFilter(request, response);  // Skip JWT, maybe public endpoint
        return;
    }
    
    // 2. Extract JWT token
    final String jwt = authHeader.substring(7);  // Remove "Bearer " prefix
    final String username = jwtService.extractUsername(jwt);
    
    // 3. Load user and validate token
    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        
        if (jwtService.isTokenValid(jwt, userDetails)) {
            // 4. Create authentication object
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()  // Roles
            );
            
            // 5. Set authentication in Spring Security context
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
    }
    
    // 6. Continue to next filter
    filterChain.doFilter(request, response);
}
```

**Step-by-Step**:

1. **Extract Header**
   ```
   Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
   ```

2. **Extract Token**
   ```
   Remove "Bearer " → eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
   ```

3. **Extract Username**
   ```
   Decode JWT → "john"
   ```

4. **Load User**
   ```
   Database query → User(id=5, username=john, role=USER)
   ```

5. **Validate Token**
   ```
   Check signature, expiration, username match → Valid!
   ```

6. **Set Authentication**
   ```
   Spring Security now knows: "This request is from user 'john' with role USER"
   ```

7. **Continue**
   ```
   Request reaches controller
   Controller can access: @AuthenticationPrincipal User currentUser
   ```

**If Token Invalid**:
- Authentication not set
- SecurityFilterChain sees unauthenticated request
- If endpoint requires auth → Returns 403 Forbidden
- If endpoint public → Continues normally

---

### 5. GlobalExceptionHandler.java

**Purpose**: Convert Java exceptions to clean HTTP responses

**What is @ControllerAdvice?**
- Intercepts exceptions thrown by controllers
- Converts to appropriate HTTP responses
- Provides consistent error format

**Without Exception Handler**:
```
Exception → Stack trace → 500 Internal Server Error → Ugly error page
```

**With Exception Handler**:
```
Exception → Caught → Clean JSON response → Frontend shows user-friendly message
```

**Key Methods**:

#### A. UsernameNotFoundException
```java
@ExceptionHandler(UsernameNotFoundException.class)
public ResponseEntity<String> handleUsernameNotFound(UsernameNotFoundException ex) {
    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)  // 404
        .body(ex.getMessage());         // "User not found"
}
```

**When Thrown**:
```java
// In UserService
User user = userRepository.findById(id)
    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
```

**Response**:
```
HTTP 404 Not Found
Body: "User not found"
```

#### B. IllegalStateException
```java
@ExceptionHandler(IllegalStateException.class)
public ResponseEntity<String> handleIllegalState(IllegalStateException ex) {
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)  // 400
        .body(ex.getMessage());
}
```

**When Thrown**:
```java
// In AuthService
if (userRepository.existsByUsername(username)) {
    throw new IllegalStateException("Username '" + username + "' is already taken");
}
```

**Response**:
```
HTTP 400 Bad Request
Body: "Username 'john' is already taken"
```

#### C. MethodArgumentNotValidException
```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<Map<String, String>> handleValidationExceptions(
    MethodArgumentNotValidException ex
) {
    Map<String, String> errors = new HashMap<>();
    
    ex.getBindingResult().getFieldErrors().forEach(error -> {
        errors.put(error.getField(), error.getDefaultMessage());
    });
    
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(errors);
}
```

**When Thrown**:
- `@Valid` validation fails in controller
- DTO has `@NotBlank`, `@Email`, etc.

**Example**:
```java
// Frontend sends invalid data
{
  "username": "",           // Blank!
  "email": "not-an-email",  // Invalid format!
  "password": "123"         // Too short!
}
```

**Response**:
```json
{
  "username": "Username is required",
  "email": "Email must be valid",
  "password": "Password must be at least 6 characters"
}
```

**Frontend can show these errors next to each field!**

#### D. Generic Exception
```java
@ExceptionHandler(Exception.class)
public ResponseEntity<String> handleGenericException(Exception ex) {
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)  // 500
        .body("An error occurred: " + ex.getMessage());
}
```

**Catches all other exceptions**:
- NullPointerException
- IllegalArgumentException
- Database errors
- Unexpected errors

**Production Best Practice**:
- Log full error (stack trace) to file
- Return generic message to user (don't expose internals)
```java
logger.error("Unexpected error", ex);
return ResponseEntity
    .status(HttpStatus.INTERNAL_SERVER_ERROR)
    .body("An error occurred. Please try again later.");
```

---

## 🎯 Configuration Flow Summary

### 1. Application Startup
```
Spring Boot starts
  → Loads @Configuration classes
  → Creates @Bean objects
  → Injects beans where needed
  → Starts embedded Tomcat server
  → Application ready!
```

### 2. Request Flow
```
Request arrives
  → JwtAuthFilter extracts & validates token
  → Sets authentication in SecurityContext
  → SecurityFilterChain checks permissions
  → If allowed → Controller handles request
  → If exception → GlobalExceptionHandler catches
  → Response sent to frontend
```

### 3. Authentication Flow
```
User logs in
  → AuthController.login()
  → AuthService.authenticate()
  → AuthenticationManager.authenticate()
  → AuthenticationProvider:
     - UserDetailsService loads user
     - PasswordEncoder checks password
  → If valid → JwtService generates tokens
  → Tokens sent to frontend
```

---

## 🔐 Security in Action

### Example: Create Post

**Request**:
```
POST /api/posts
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "title": "My Post",
  "content": "Content here"
}
```

**Filter Chain**:
1. **JwtAuthFilter**:
   - Extracts token
   - Validates: `jwtService.isTokenValid()`
   - Loads user: `userDetailsService.loadUserByUsername("john")`
   - Sets auth: `SecurityContextHolder.setAuthentication(auth)`

2. **SecurityFilterChain**:
   - Checks: `/api/posts` requires authentication?
   - Yes! User authenticated?
   - Yes! Allow request

3. **Controller**:
   - `@AuthenticationPrincipal User currentUser` → Gets user from SecurityContext
   - Creates post with `currentUser` as author

4. **Response**:
   ```json
   {
     "id": 1,
     "title": "My Post",
     "authorId": 5,
     "authorUsername": "john"
   }
   ```

**If Token Missing/Invalid**:
```
403 Forbidden
Body: "Access Denied"
```

---

## 📚 Summary

Configuration files wire your application together:

1. **SecurityConfig**: Who can access what?
2. **JwtService**: Generate and validate tokens
3. **ApplicationConfig**: Core beans (AuthenticationManager, etc.)
4. **JwtAuthFilter**: Intercept requests, validate tokens
5. **GlobalExceptionHandler**: Clean error responses

**All work together** to provide secure, well-structured authentication!
