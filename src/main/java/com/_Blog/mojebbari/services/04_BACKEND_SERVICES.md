# Backend Services - Business Logic Layer

## 📖 What are Services?

**Services** contain the **business logic** of your application. They sit between Controllers (which handle HTTP requests) and Repositories (which access the database).

Think of Services as the "brain" of your application where:
- Data validation happens
- Business rules are enforced
- Complex operations are coordinated
- Data transformations occur

## 🏗️ Layered Architecture

```
Controller (HTTP Layer)
    ↓
Service (Business Logic Layer) ← YOU ARE HERE
    ↓
Repository (Data Access Layer)
    ↓
Database
```

## 🔧 Key Annotations

### @Service
Marks a class as a service component. Spring will create and manage it.

### @RequiredArgsConstructor (Lombok)
Generates a constructor with required fields (final fields).
Enables **Dependency Injection**.

### @Transactional
Wraps method in a database transaction:
- If method succeeds → COMMIT
- If method throws exception → ROLLBACK

```java
@Transactional(readOnly = true)  // For SELECT queries (optimization)
@Transactional                    // For INSERT/UPDATE/DELETE
```

---

## 📁 Services in this Project

### 1. AuthService.java
**Purpose**: Handle user authentication (register, login, token refresh)

**Dependencies**:
- `UserRepository` - Save/find users
- `PasswordEncoder` - Hash passwords
- `JwtService` - Generate JWT tokens
- `AuthenticationManager` - Validate credentials

**Methods**:

#### `register(RegisterRequest request)`
**What it does**: Creates a new user account

**Steps**:
1. Check if username already exists → throw error if yes
2. Check if email already exists → throw error if yes
3. Hash the password using BCrypt
4. Create User object with role = USER
5. Save to database
6. Generate access token and refresh token
7. Return tokens

**Code Flow**:
```java
// 1. Validation
if (userRepository.existsByUsername(request.getUsername())) {
    throw new IllegalStateException("Username already taken");
}

// 2. Create user
User user = User.builder()
    .username(request.getUsername())
    .email(request.getEmail())
    .password(passwordEncoder.encode(request.getPassword())) // Hash it!
    .role(Role.USER)
    .build();

// 3. Save
userRepository.save(user);

// 4. Generate tokens
String accessToken = jwtService.generateToken(user);
String refreshToken = jwtService.generateRefreshToken(user);

// 5. Return
return AuthenticationResponse.builder()
    .token(accessToken)
    .refreshToken(refreshToken)
    .build();
```

**Why hash passwords?**
- Never store plain text passwords!
- BCrypt is a one-way hash (can't be reversed)
- Even if database is hacked, passwords are safe

#### `registerAdmin(RegisterRequest request)`
Same as `register()` but sets role = ADMIN

**⚠️ Security Warning**: In production, this endpoint should:
- Be protected by authentication
- Require existing admin approval
- Or be removed entirely

#### `login(LoginRequest request)`
**What it does**: Authenticates a user and returns tokens

**Steps**:
1. Use `AuthenticationManager` to validate username/email and password
2. If invalid → throws `BadCredentialsException` (handled by GlobalExceptionHandler)
3. If valid → fetch user from database
4. Generate new access token and refresh token
5. Return tokens

**Code Flow**:
```java
// 1. Validate credentials (Spring Security does this)
authenticationManager.authenticate(
    new UsernamePasswordAuthenticationToken(
        request.getIdentifier(),  // username or email
        request.getPassword()
    )
);
// If wrong password, this throws BadCredentialsException

// 2. Find user
User user = userRepository.findByEmailOrUsername(
    request.getIdentifier(), 
    request.getIdentifier()
).orElseThrow(() -> new EntityNotFoundException("User not found"));

// 3. Generate tokens
String accessToken = jwtService.generateToken(user);
String refreshToken = jwtService.generateRefreshToken(user);

// 4. Return
return AuthenticationResponse.builder()
    .token(accessToken)
    .refreshToken(refreshToken)
    .build();
```

**What is AuthenticationManager?**
- Part of Spring Security
- Validates credentials against database
- Uses the `UserDetailsService` we configured

#### `refreshToken(String refreshToken)`
**What it does**: Generate new access token using refresh token

**Why needed?**
- Access tokens expire quickly (10 hours)
- Refresh tokens last longer (7 days)
- User doesn't have to login again

**Steps**:
1. Extract username from refresh token
2. Validate token is not expired
3. Find user
4. Generate new access token
5. Return new token (keep same refresh token)

**Used in**: When frontend detects access token is expired

---

### 2. PostService.java
**Purpose**: Handle post-related operations (create, update, delete, fetch)

**Dependencies**:
- `PostRepository` - Post database operations
- `UserRepository` - Get post author
- `LikeRepository` - Count likes
- `CommentRepository` - Count comments

**Methods**:

#### `getAllPosts()`
**What it does**: Get all posts for home feed

**Steps**:
1. Fetch all posts ordered by newest first
2. For each post:
   - Count likes
   - Count comments
   - Map to PostResponse DTO
3. Return list of PostResponse

**Why use DTO (Data Transfer Object)?**
- Don't send entire User object (includes password!)
- Only send what frontend needs
- Reduce data transferred

#### `getPostById(Long id)`
**What it does**: Get a single post with full details

**Steps**:
1. Find post by ID → throw error if not found
2. Count likes and comments
3. Map to PostResponse
4. Return post

#### `createPost(CreatePostRequest request, Long userId)`
**What it does**: Create a new blog post

**Steps**:
1. Find user by ID → throw error if not found
2. Create Post object
3. Save to database
4. Return created post

**Code Example**:
```java
// 1. Get user
User user = userRepository.findById(userId)
    .orElseThrow(() -> new EntityNotFoundException("User not found"));

// 2. Create post
Post post = Post.builder()
    .title(request.getTitle())
    .content(request.getContent())
    .category(request.getCategory())
    .mediaUrl(request.getMediaUrl())
    .user(user)
    .build();

// 3. Save
Post savedPost = postRepository.save(post);

// 4. Map to response
return mapToResponse(savedPost);
```

#### `updatePost(Long id, UpdatePostRequest request, Long userId)`
**What it does**: Update an existing post

**Security Check**: Only the post author can update it!

**Steps**:
1. Find post → throw error if not found
2. Check if current user is the author → throw error if not
3. Update fields
4. Save
5. Return updated post

**Code Example**:
```java
// 1. Find post
Post post = postRepository.findById(id)
    .orElseThrow(() -> new EntityNotFoundException("Post not found"));

// 2. Security check
if (!post.getUser().getId().equals(userId)) {
    throw new AccessDeniedException("You can only edit your own posts");
}

// 3. Update
post.setTitle(request.getTitle());
post.setContent(request.getContent());
post.setCategory(request.getCategory());

// 4. Save
Post updated = postRepository.save(post);

// 5. Return
return mapToResponse(updated);
```

#### `deletePost(Long id, Long userId)`
**What it does**: Delete a post

**Security Check**: Only the post author or admin can delete it!

**Steps**:
1. Find post
2. Check if current user is author or admin
3. Delete from database

**Cascade Delete**: When post is deleted, all related comments and likes are automatically deleted too (configured in Post model with `CascadeType.ALL`)

#### `getUserPosts(Long userId)`
**What it does**: Get all posts by a specific user (for profile page)

**Steps**:
1. Find posts by user ID
2. Map each to PostResponse
3. Return list

---

### 3. CommentService.java
**Purpose**: Handle comment operations

**Methods**:

#### `getCommentsByPostId(Long postId)`
**What it does**: Get all comments for a post

**Steps**:
1. Check if post exists
2. Fetch comments ordered by oldest first (conversation flow)
3. Map to CommentResponse
4. Return list

#### `createComment(CreateCommentRequest request, Long userId)`
**What it does**: Add a comment to a post

**Steps**:
1. Find post → throw error if not found
2. Find user → throw error if not found
3. Create Comment object
4. Save
5. Return created comment

#### `updateComment(Long id, UpdateCommentRequest request, Long userId)`
**What it does**: Edit a comment

**Security**: Only comment author can edit

**Steps**:
1. Find comment
2. Check if user is author
3. Update text
4. Update timestamp
5. Save
6. Return

#### `deleteComment(Long id, Long userId)`
**What it does**: Delete a comment

**Security**: Only comment author or post author can delete

**Steps**:
1. Find comment
2. Check permissions
3. Delete

---

### 4. LikeService.java
**Purpose**: Handle post likes/unlikes

**Methods**:

#### `toggleLike(Long postId, Long userId)`
**What it does**: Like or unlike a post (toggle)

**Steps**:
1. Check if post exists
2. Check if user already liked it:
   - If YES → Remove like (unlike)
   - If NO → Add like
3. Return result

**Code Example**:
```java
// Check if already liked
Optional<Like> existingLike = likeRepository.findByUserIdAndPostId(userId, postId);

if (existingLike.isPresent()) {
    // Unlike: delete the like
    likeRepository.delete(existingLike.get());
    return LikeResponse.builder()
        .liked(false)
        .message("Post unliked")
        .build();
} else {
    // Like: create new like
    Like like = Like.builder()
        .user(user)
        .post(post)
        .build();
    likeRepository.save(like);
    return LikeResponse.builder()
        .liked(true)
        .message("Post liked")
        .build();
}
```

#### `hasUserLikedPost(Long postId, Long userId)`
**What it does**: Check if user liked a post

**Used in**: To show filled/unfilled heart icon

**Returns**: boolean

---

### 5. SubscriptionService.java
**Purpose**: Handle follow/unfollow functionality

**Methods**:

#### `followUser(Long followingId, Long followerId)`
**What it does**: User A follows User B

**Validation**:
- Can't follow yourself
- Can't follow same user twice

**Steps**:
1. Check if already following → throw error
2. Check if trying to follow self → throw error
3. Find both users
4. Create Subscription
5. Save
6. Return success

#### `unfollowUser(Long followingId, Long followerId)`
**What it does**: User A unfollows User B

**Steps**:
1. Find subscription
2. Delete it
3. Return success

#### `getUserFollowers(Long userId)`
**What it does**: Get list of users following this user

**Steps**:
1. Find all subscriptions where following = userId
2. Extract follower users
3. Map to UserSummary DTO
4. Return list

#### `getUserFollowing(Long userId)`
**What it does**: Get list of users this user follows

**Steps**:
1. Find all subscriptions where follower = userId
2. Extract following users
3. Map to UserSummary
4. Return list

#### `isFollowing(Long followerId, Long followingId)`
**What it does**: Check if user A follows user B

**Returns**: boolean

**Used in**: Show "Follow" or "Unfollow" button

---

### 6. ReportService.java
**Purpose**: Handle content reporting

**Methods**:

#### `createReport(CreateReportRequest request, Long reporterId)`
**What it does**: User reports content (post, comment, or user)

**Validation**:
- Can't report own content
- Can't report same content twice

**Steps**:
1. Find reporter user
2. Create Report object with status = PENDING
3. Based on report type:
   - POST: Find post, check not own post, check not already reported
   - COMMENT: Find comment, check not own comment, check not already reported
   - USER: Find user, check not self, check not already reported
4. Save report
5. Return success

**Code Example** (for reporting a user):
```java
if (request.getReportType() == ReportType.USER) {
    // Check if already reported
    if (reportRepository.existsByReporterIdAndReportedUserId(reporterId, request.getContentId())) {
        throw new IllegalStateException("You have already reported this user");
    }
    
    // Find reported user
    User reportedUser = userRepository.findById(request.getContentId())
        .orElseThrow(() -> new EntityNotFoundException("User not found"));
    
    // Can't report yourself
    if (reportedUser.getId().equals(reporterId)) {
        throw new IllegalStateException("You cannot report yourself");
    }
    
    report.setReportedUser(reportedUser);
}
```

#### `getAllReports()` (Admin only)
**What it does**: Get all reports for admin dashboard

**Steps**:
1. Fetch all reports ordered by newest
2. Map to ReportResponse (includes reporter, content preview, status)
3. Return list

#### `getReportsByStatus(ReportStatus status)` (Admin only)
**What it does**: Filter reports by status

**Used in**: Admin dashboard tabs (Pending, Reviewed, Resolved, Dismissed)

#### `updateReportStatus(Long reportId, UpdateReportStatusRequest request, Long adminId)` (Admin only)
**What it does**: Admin updates report status

**Steps**:
1. Find report
2. Find admin user
3. Update status
4. Add admin notes
5. Set reviewedBy = admin
6. Set reviewedAt = now
7. Save
8. Return updated report

#### `getPendingReportsCount()` (Admin only)
**What it does**: Count pending reports

**Used in**: Admin navbar badge showing number of pending reports

---

### 7. UserService.java
**Purpose**: Handle user profile operations

**Methods**:

#### `getUserProfile(String username)`
**What it does**: Get user profile information

**Steps**:
1. Find user by username → throw error if not found
2. Count user's posts
3. Count followers
4. Count following
5. Map to UserProfileResponse
6. Return profile

**UserProfileResponse includes**:
- Basic info (username, email, bio, pictures)
- Statistics (posts count, followers count, following count)
- Join date

#### `updateProfile(UpdateProfileRequest request, Long userId)`
**What it does**: User updates their own profile

**Updatable fields**:
- Bio
- Profile picture
- Cover image

**Steps**:
1. Find user
2. Update fields
3. Save
4. Return updated profile

#### `uploadProfilePicture(MultipartFile file, Long userId)`
**What it does**: Upload and set profile picture

**Steps**:
1. Validate file (type, size)
2. Generate unique filename
3. Save file to disk
4. Update user's profilePicture field
5. Return file path

**File Handling**:
```java
// Generate unique name
String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

// Save to uploads directory
Path filePath = Paths.get(uploadDir, fileName);
Files.copy(file.getInputStream(), filePath);

// Update user
user.setProfilePicture("/uploads/" + fileName);
```

---

## 🎯 Service Design Principles

### 1. Single Responsibility
Each service handles one domain (Auth, Post, Comment, etc.)

### 2. Dependency Injection
Services receive dependencies through constructor:
```java
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
}
```

### 3. Transaction Management
Use `@Transactional` for data consistency:
```java
@Transactional
public void deletePost(Long id) {
    // If this fails, nothing is deleted
    postRepository.deleteById(id);
}
```

### 4. Error Handling
Throw meaningful exceptions:
```java
if (user == null) {
    throw new EntityNotFoundException("User not found");
}

if (!post.getUser().getId().equals(userId)) {
    throw new AccessDeniedException("Not authorized");
}
```

### 5. DTO Mapping
Never return entities directly:
```java
private PostResponse mapToResponse(Post post) {
    return PostResponse.builder()
        .id(post.getId())
        .title(post.getTitle())
        .authorUsername(post.getUser().getUsername())
        // ... only what frontend needs
        .build();
}
```

---

## 📊 Service Layer Benefits

1. **Separation of Concerns**: Business logic separate from HTTP handling
2. **Reusability**: Services can be used by multiple controllers
3. **Testability**: Easy to unit test business logic
4. **Security**: Enforce permissions here, not in controllers
5. **Transaction Management**: Automatic rollback on errors

---

## 📚 Summary

Services are the **heart of your application**. They:
1. Implement business rules
2. Coordinate multiple repositories
3. Validate data and permissions
4. Transform data between layers
5. Handle transactions

Every user action flows through a service!
