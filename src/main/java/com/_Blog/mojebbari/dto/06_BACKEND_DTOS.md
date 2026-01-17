# Backend DTOs - Data Transfer Objects

## 📖 What are DTOs?

**DTOs (Data Transfer Objects)** are simple Java classes used to transfer data between different layers of your application, especially between:
- **Frontend ↔ Backend** (HTTP requests/responses)
- **Controller ↔ Service** layers

Think of DTOs as **contracts** that define exactly what data is sent and received.

## 🤔 Why Use DTOs Instead of Entities?

### Problem with Sending Entities Directly:
```java
// BAD - Sending User entity to frontend
@GetMapping("/user/{id}")
public User getUser(@PathVariable Long id) {
    return userRepository.findById(id).get();
}
```

**Issues**:
1. **Security**: Sends password hash to frontend!
2. **Unnecessary Data**: Sends all relationships (posts, followers, etc.)
3. **Circular References**: User → Post → User → Post... (infinite loop in JSON)
4. **No Control**: Can't customize what fields to send

### Solution with DTOs:
```java
// GOOD - Using DTO
@GetMapping("/user/{id}")
public UserResponse getUser(@PathVariable Long id) {
    User user = userRepository.findById(id).get();
    return UserResponse.builder()
        .id(user.getId())
        .username(user.getUsername())
        .email(user.getEmail())
        // NO PASSWORD!
        .build();
}
```

**Benefits**:
1. **Security**: Only send what frontend needs
2. **Performance**: Less data transferred
3. **Flexibility**: Different DTOs for different use cases
4. **Validation**: Add validation rules

---

## 🔧 Key Annotations

### Lombok Annotations
- `@Data` - Generates getters, setters, toString, equals, hashCode
- `@Builder` - Enables builder pattern: `UserResponse.builder().username("john").build()`
- `@NoArgsConstructor` - Generates no-argument constructor (needed for JSON deserialization)
- `@AllArgsConstructor` - Generates constructor with all fields

### Validation Annotations (jakarta.validation.constraints)
- `@NotBlank` - String must not be null or empty
- `@NotNull` - Value must not be null
- `@Email` - Must be valid email format
- `@Size(min=3, max=50)` - String length constraints
- `@Min(1)` - Minimum numeric value
- `@Max(100)` - Maximum numeric value
- `@Pattern(regexp="...")` - Custom regex pattern

Example:
```java
@Data
@Builder
public class RegisterRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be 3-50 characters")
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}
```

When you use `@Valid` in controller, Spring automatically validates these rules!

---

## 📁 DTOs in this Project

### Request DTOs (Frontend → Backend)

#### 1. RegisterRequest.java
**Purpose**: User registration data

**Fields**:
```java
@NotBlank(message = "Username is required")
@Size(min = 3, max = 50)
private String username;

@NotBlank(message = "Email is required")
@Email(message = "Email must be valid")
private String email;

@NotBlank(message = "Password is required")
@Size(min = 6, message = "Password must be at least 6 characters")
private String password;
```

**Used in**: `POST /api/auth/register`

**Frontend sends**:
```json
{
  "username": "john",
  "email": "john@example.com",
  "password": "password123"
}
```

**Validation**:
- Username: 3-50 characters, required
- Email: Valid format, required
- Password: At least 6 characters, required

---

#### 2. LoginRequest.java
**Purpose**: User login credentials

**Fields**:
```java
@NotBlank(message = "Username or Email is required")
private String identifier;  // Can be username OR email

@NotBlank(message = "Password is required")
private String password;
```

**Used in**: `POST /api/auth/login`

**Frontend sends**:
```json
{
  "identifier": "john",  // or "john@example.com"
  "password": "password123"
}
```

**Why "identifier" instead of "email"?**
- Allows login with username OR email
- More flexible for users

---

#### 3. CreatePostRequest.java
**Purpose**: Data to create a new post

**Fields**:
```java
@NotBlank(message = "Title is required")
@Size(max = 255, message = "Title too long")
private String title;

@NotBlank(message = "Content is required")
private String content;

private String category;  // Optional

private String mediaUrl;  // Optional
```

**Used in**: `POST /api/posts`

**Frontend sends**:
```json
{
  "title": "My Blog Post",
  "content": "This is the content...",
  "category": "Technology",
  "mediaUrl": "/uploads/image.jpg"
}
```

**Validation**:
- Title: Required, max 255 characters
- Content: Required
- Category: Optional
- MediaUrl: Optional

---

#### 4. UpdatePostRequest.java
**Purpose**: Data to update an existing post

**Same fields as CreatePostRequest**

**Why separate DTO?**
- Might have different validation rules
- Future-proof: can add update-specific fields
- Clear intent in code

**Used in**: `PUT /api/posts/{id}`

---

#### 5. CreateCommentRequest.java
**Purpose**: Data to create a comment

**Fields**:
```java
@NotNull(message = "Post ID is required")
private Long postId;

@NotBlank(message = "Comment text is required")
@Size(max = 500, message = "Comment too long")
private String text;
```

**Used in**: `POST /api/comments`

**Frontend sends**:
```json
{
  "postId": 5,
  "text": "Great post!"
}
```

---

#### 6. UpdateCommentRequest.java
**Purpose**: Data to update a comment

**Fields**:
```java
@NotBlank(message = "Comment text is required")
@Size(max = 500)
private String text;
```

**Used in**: `PUT /api/comments/{id}`

---

#### 7. UpdateProfileRequest.java
**Purpose**: Data to update user profile

**Fields**:
```java
@Size(max = 500, message = "Bio too long")
private String bio;

private String profilePicture;

private String coverImage;
```

**Used in**: `PUT /api/users/profile`

**Frontend sends**:
```json
{
  "bio": "Software developer from NYC",
  "profilePicture": "/uploads/pic.jpg",
  "coverImage": "/uploads/cover.jpg"
}
```

**All fields optional** - user can update any combination

---

#### 8. CreateReportRequest.java
**Purpose**: Data to report content

**Fields**:
```java
@NotNull(message = "Report type is required")
private ReportType reportType;  // POST, COMMENT, or USER

@NotNull(message = "Content ID is required")
private Long contentId;  // ID of post, comment, or user

@NotNull(message = "Reason is required")
private ReportReason reason;  // SPAM, HARASSMENT, etc.

@Size(max = 1000, message = "Description too long")
private String description;  // Optional additional details
```

**Used in**: `POST /api/reports`

**Frontend sends**:
```json
{
  "reportType": "POST",
  "contentId": 5,
  "reason": "SPAM",
  "description": "This is promotional content"
}
```

---

#### 9. UpdateReportStatusRequest.java
**Purpose**: Admin updates report status

**Fields**:
```java
@NotNull(message = "Status is required")
private ReportStatus status;  // PENDING, REVIEWED, RESOLVED, DISMISSED

@Size(max = 1000, message = "Admin notes too long")
private String adminNotes;  // Optional admin comments
```

**Used in**: `PUT /api/reports/{id}/status` (Admin only)

**Frontend sends**:
```json
{
  "status": "RESOLVED",
  "adminNotes": "User has been warned and content removed"
}
```

---

### Response DTOs (Backend → Frontend)

#### 1. AuthenticationResponse.java
**Purpose**: Response after successful login/register

**Fields**:
```java
private String token;         // Access token (JWT)
private String refreshToken;  // Refresh token (JWT)
private String message;       // Optional success message
```

**Backend sends**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "message": "Login successful"
}
```

**Frontend uses**:
- Stores tokens in localStorage
- Includes token in Authorization header for future requests

---

#### 2. PostResponse.java
**Purpose**: Complete post information

**Fields**:
```java
private Long id;
private String title;
private String content;
private String category;
private String mediaUrl;
private Long authorId;
private String authorUsername;
private String authorProfilePicture;
private Long likesCount;
private Long commentsCount;
private LocalDateTime createdAt;
```

**Backend sends**:
```json
{
  "id": 1,
  "title": "My Blog Post",
  "content": "This is the content...",
  "category": "Technology",
  "mediaUrl": "/uploads/image.jpg",
  "authorId": 5,
  "authorUsername": "john",
  "authorProfilePicture": "/uploads/john.jpg",
  "likesCount": 25,
  "commentsCount": 10,
  "createdAt": "2024-01-15T10:30:00"
}
```

**Why include author information?**
- Frontend can display author without extra API call
- Denormalization for performance

**Why include counts?**
- Avoid extra API calls to get counts
- Can display "25 likes, 10 comments" immediately

---

#### 3. CommentResponse.java
**Purpose**: Comment information

**Fields**:
```java
private Long id;
private String text;
private Long userId;
private String username;
private String userProfilePicture;
private Long postId;
private LocalDateTime createdAt;
private LocalDateTime updatedAt;
```

**Backend sends**:
```json
{
  "id": 1,
  "text": "Great post!",
  "userId": 10,
  "username": "jane",
  "userProfilePicture": "/uploads/jane.jpg",
  "postId": 5,
  "createdAt": "2024-01-15T11:00:00",
  "updatedAt": "2024-01-15T11:05:00"
}
```

---

#### 4. UserProfileResponse.java
**Purpose**: Complete user profile information

**Fields**:
```java
private Long id;
private String username;
private String email;
private String bio;
private String profilePicture;
private String coverImage;
private Long postsCount;
private Long followersCount;
private Long followingCount;
private LocalDateTime createdAt;
```

**Backend sends**:
```json
{
  "id": 5,
  "username": "john",
  "email": "john@example.com",
  "bio": "Software developer",
  "profilePicture": "/uploads/john.jpg",
  "coverImage": "/uploads/cover.jpg",
  "postsCount": 15,
  "followersCount": 50,
  "followingCount": 30,
  "createdAt": "2024-01-01T00:00:00"
}
```

**Used in**: User profile page

---

#### 5. UserSummary.java
**Purpose**: Minimal user info for lists (followers, following)

**Fields**:
```java
private Long id;
private String username;
private String bio;
private String profilePicture;
private boolean isFollowing;  // Does current user follow them?
```

**Backend sends**:
```json
{
  "id": 10,
  "username": "jane",
  "bio": "Designer",
  "profilePicture": "/uploads/jane.jpg",
  "isFollowing": true
}
```

**Why separate from UserProfileResponse?**
- Lighter payload for lists
- Only essential information
- Better performance when loading many users

---

#### 6. ReportResponse.java
**Purpose**: Report information for admin dashboard

**Fields**:
```java
private Long id;
private ReportType reportType;
private ReportReason reason;
private String description;
private ReportStatus status;
private String reporterUsername;
private Long contentId;
private String contentPreview;      // First 100 chars of content
private String contentAuthor;        // Author of reported content
private String adminNotes;
private String reviewedByUsername;
private LocalDateTime createdAt;
private LocalDateTime reviewedAt;
```

**Backend sends**:
```json
{
  "id": 1,
  "reportType": "POST",
  "reason": "SPAM",
  "description": "Promotional content",
  "status": "PENDING",
  "reporterUsername": "john",
  "contentId": 5,
  "contentPreview": "Buy my product! Click here...",
  "contentAuthor": "spammer",
  "adminNotes": null,
  "reviewedByUsername": null,
  "createdAt": "2024-01-15T10:00:00",
  "reviewedAt": null
}
```

**Used in**: Admin dashboard

---

#### 7. LikeResponse.java
**Purpose**: Response after like/unlike action

**Fields**:
```java
private boolean liked;   // true = liked, false = unliked
private String message;  // "Post liked" or "Post unliked"
```

**Backend sends**:
```json
{
  "liked": true,
  "message": "Post liked"
}
```

**Used in**: Frontend updates heart icon state

---

## 🎯 DTO Design Patterns

### 1. Request/Response Separation
```
CreatePostRequest  (Frontend → Backend)
UpdatePostRequest  (Frontend → Backend)
PostResponse       (Backend → Frontend)
```

**Benefits**:
- Clear intent
- Different validation rules
- Future-proof

### 2. Summary vs Full DTOs
```
UserSummary        (minimal info for lists)
UserProfileResponse (complete profile)
```

**Benefits**:
- Performance
- Right amount of data for each use case

### 3. Embedded Information
```java
// PostResponse includes author info
private String authorUsername;
private String authorProfilePicture;
```

**Benefits**:
- Reduce API calls
- Frontend gets everything it needs in one request

---

## 🔄 Entity ↔ DTO Mapping

### Manual Mapping (Current approach)
```java
private PostResponse mapToResponse(Post post) {
    return PostResponse.builder()
        .id(post.getId())
        .title(post.getTitle())
        .content(post.getContent())
        .authorId(post.getUser().getId())
        .authorUsername(post.getUser().getUsername())
        .likesCount(likeRepository.countByPostId(post.getId()))
        .commentsCount(commentRepository.countByPostId(post.getId()))
        .createdAt(post.getCreatedAt())
        .build();
}
```

**Pros**:
- Full control
- Easy to understand
- No external dependency

**Cons**:
- Verbose
- Manual work

### Alternative: MapStruct (Advanced)
A library that generates mapping code automatically. Not used in this project, but good to know!

---

## ✅ Validation Flow

### 1. Define Rules in DTO
```java
@Data
public class RegisterRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50)
    private String username;
    
    @Email(message = "Invalid email format")
    private String email;
}
```

### 2. Use @Valid in Controller
```java
@PostMapping("/register")
public ResponseEntity<AuthenticationResponse> register(
    @Valid @RequestBody RegisterRequest request  // ← @Valid triggers validation
) {
    return ResponseEntity.ok(authService.register(request));
}
```

### 3. Spring Validates Automatically
If validation fails:
- Spring throws `MethodArgumentNotValidException`
- `GlobalExceptionHandler` catches it
- Returns 400 Bad Request with error details

**Example error response**:
```json
{
  "username": "Username must be 3-50 characters",
  "email": "Invalid email format"
}
```

---

## 📚 Summary

DTOs are essential for:
1. **Security** - Don't expose sensitive data
2. **Performance** - Transfer only needed data
3. **Flexibility** - Different views of same entity
4. **Validation** - Enforce data quality
5. **Decoupling** - Frontend and backend can evolve independently

Every API request and response uses DTOs!
