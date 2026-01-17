# Backend Controllers - HTTP Request Handlers

## 📖 What are Controllers?

**Controllers** are the entry point to your application. They:
- Receive HTTP requests from clients (frontend, mobile app, etc.)
- Parse request data (JSON, form data, URL parameters)
- Call appropriate service methods
- Return HTTP responses (JSON, status codes)

Think of Controllers as **receptionists** who:
- Listen for visitors (HTTP requests)
- Direct them to the right department (Service)
- Send back responses

## 🏗️ Request Flow

```
Frontend (Angular)
    ↓ HTTP Request (GET, POST, PUT, DELETE)
Controller ← YOU ARE HERE
    ↓ Call method
Service (Business Logic)
    ↓ Query database
Repository
    ↓
Database
```

## 🔧 Key Annotations

### @RestController
Combination of `@Controller` + `@ResponseBody`
- Marks class as a REST API controller
- Automatically converts return values to JSON

### @RequestMapping("/api/path")
Defines base URL for all methods in controller
Example: `@RequestMapping("/api/posts")`

### HTTP Method Annotations
- `@GetMapping` - Read data (SELECT)
- `@PostMapping` - Create data (INSERT)
- `@PutMapping` - Update data (UPDATE)
- `@DeleteMapping` - Delete data (DELETE)

### Parameter Annotations
- `@PathVariable` - Get value from URL path
  - Example: `/posts/{id}` → `@PathVariable Long id`
- `@RequestBody` - Get JSON from request body
  - Example: `@RequestBody CreatePostRequest request`
- `@RequestParam` - Get query parameters
  - Example: `/posts?category=tech` → `@RequestParam String category`
- `@RequestHeader` - Get header value
  - Example: `@RequestHeader("Authorization") String token`

### Validation
- `@Valid` - Validate request body using annotations in DTO
- `@NotBlank`, `@Email`, `@Size` - Validation rules in DTO

### Security
- `@PreAuthorize("hasRole('ADMIN')")` - Only admins can access
- `@PreAuthorize("hasAnyRole('USER', 'ADMIN')")` - Users or admins

---

## 📁 Controllers in this Project

### 1. AuthController.java
**Base URL**: `/api/auth`

**Purpose**: Handle user authentication (register, login, refresh token)

**No Security**: These endpoints are public (anyone can access)

**Endpoints**:

#### `POST /api/auth/register`
**Purpose**: Register a new user

**Request Body**:
```json
{
  "username": "john",
  "email": "john@example.com",
  "password": "password123"
}
```

**Response** (200 OK):
```json
{
  "token": "eyJhbGc...",
  "refreshToken": "eyJhbGc..."
}
```

**Errors**:
- 400 Bad Request - Validation failed (email format, password length)
- 409 Conflict - Username or email already exists

**Code**:
```java
@PostMapping("/register")
public ResponseEntity<AuthenticationResponse> register(
    @Valid @RequestBody RegisterRequest request
) {
    return ResponseEntity.ok(authService.register(request));
}
```

**What happens**:
1. `@Valid` checks request fields (annotations in RegisterRequest)
2. If valid, calls `authService.register()`
3. Returns 200 OK with tokens
4. If invalid, Spring returns 400 automatically

#### `POST /api/auth/register-admin`
**Purpose**: Register an admin user (FOR DEVELOPMENT ONLY!)

**⚠️ Security Warning**: In production:
- Remove this endpoint, OR
- Protect with existing admin authentication, OR
- Add invitation code system

**Same as register but creates user with ADMIN role**

#### `POST /api/auth/login`
**Purpose**: Login existing user

**Request Body**:
```json
{
  "identifier": "john",  // Can be username OR email
  "password": "password123"
}
```

**Response** (200 OK):
```json
{
  "token": "eyJhbGc...",
  "refreshToken": "eyJhbGc..."
}
```

**Errors**:
- 400 Bad Request - Validation failed
- 401 Unauthorized - Wrong username/password

**Code**:
```java
@PostMapping("/login")
public ResponseEntity<AuthenticationResponse> login(
    @Valid @RequestBody LoginRequest request
) {
    return ResponseEntity.ok(authService.login(request));
}
```

#### `POST /api/auth/refresh`
**Purpose**: Get new access token using refresh token

**Headers**:
```
Authorization: Bearer <refresh_token>
```

**Response** (200 OK):
```json
{
  "token": "new_access_token",
  "refreshToken": "same_refresh_token"
}
```

**When to use**: When access token expires (10 hours)

**Code**:
```java
@PostMapping("/refresh")
public ResponseEntity<AuthenticationResponse> refreshToken(
    @RequestHeader("Authorization") String authHeader
) {
    String refreshToken = authHeader.substring(7); // Remove "Bearer "
    return ResponseEntity.ok(authService.refreshToken(refreshToken));
}
```

---

### 2. PostController.java
**Base URL**: `/api/posts`

**Purpose**: CRUD operations for blog posts

**Security**: Most endpoints require authentication

**Endpoints**:

#### `GET /api/posts`
**Purpose**: Get all posts (home feed)

**Security**: Public (no auth needed)

**Response** (200 OK):
```json
[
  {
    "id": 1,
    "title": "My First Post",
    "content": "Hello world!",
    "authorId": 5,
    "authorUsername": "john",
    "likesCount": 10,
    "commentsCount": 3,
    "createdAt": "2024-01-15T10:30:00"
  }
]
```

**Code**:
```java
@GetMapping
public ResponseEntity<List<PostResponse>> getAllPosts() {
    return ResponseEntity.ok(postService.getAllPosts());
}
```

#### `GET /api/posts/{id}`
**Purpose**: Get a single post by ID

**Security**: Public

**URL Example**: `/api/posts/5`

**Response** (200 OK): Same structure as above

**Errors**:
- 404 Not Found - Post doesn't exist

**Code**:
```java
@GetMapping("/{id}")
public ResponseEntity<PostResponse> getPostById(@PathVariable Long id) {
    return ResponseEntity.ok(postService.getPostById(id));
}
```

**How @PathVariable works**:
- URL: `/api/posts/5`
- `{id}` in path matches `@PathVariable Long id`
- `id` variable = 5

#### `POST /api/posts`
**Purpose**: Create a new post

**Security**: Requires authentication

**Headers**:
```
Authorization: Bearer <access_token>
```

**Request Body**:
```json
{
  "title": "My New Post",
  "content": "This is the content...",
  "category": "Technology",
  "mediaUrl": "/uploads/image.jpg"
}
```

**Response** (200 OK): Created post object

**Errors**:
- 401 Unauthorized - No token or invalid token
- 400 Bad Request - Validation failed (title/content required)

**Code**:
```java
@PostMapping
public ResponseEntity<PostResponse> createPost(
    @Valid @RequestBody CreatePostRequest request,
    @AuthenticationPrincipal UserDetails userDetails
) {
    Long userId = ((User) userDetails).getId();
    return ResponseEntity.ok(postService.createPost(request, userId));
}
```

**What is @AuthenticationPrincipal?**
- Injects current authenticated user
- Extracted from JWT token automatically
- Only works if user is authenticated

#### `PUT /api/posts/{id}`
**Purpose**: Update an existing post

**Security**: Only post author can update

**URL Example**: `/api/posts/5`

**Request Body**: Same as create

**Response** (200 OK): Updated post

**Errors**:
- 404 Not Found - Post doesn't exist
- 403 Forbidden - Not your post

**Code**:
```java
@PutMapping("/{id}")
public ResponseEntity<PostResponse> updatePost(
    @PathVariable Long id,
    @Valid @RequestBody UpdatePostRequest request,
    @AuthenticationPrincipal UserDetails userDetails
) {
    Long userId = ((User) userDetails).getId();
    return ResponseEntity.ok(postService.updatePost(id, request, userId));
}
```

#### `DELETE /api/posts/{id}`
**Purpose**: Delete a post

**Security**: Only post author or admin can delete

**URL Example**: `/api/posts/5`

**Response** (204 No Content): Empty response, post deleted

**Errors**:
- 404 Not Found - Post doesn't exist
- 403 Forbidden - Not authorized

**Code**:
```java
@DeleteMapping("/{id}")
public ResponseEntity<Void> deletePost(
    @PathVariable Long id,
    @AuthenticationPrincipal UserDetails userDetails
) {
    Long userId = ((User) userDetails).getId();
    postService.deletePost(id, userId);
    return ResponseEntity.noContent().build();
}
```

**Why `ResponseEntity<Void>`?**
- `Void` means no response body
- 204 No Content status code

---

### 3. CommentController.java
**Base URL**: `/api/comments`

**Purpose**: CRUD operations for comments

**Endpoints**:

#### `GET /api/comments/post/{postId}`
**Purpose**: Get all comments for a post

**Security**: Public

**URL Example**: `/api/comments/post/5`

**Response** (200 OK):
```json
[
  {
    "id": 1,
    "text": "Great post!",
    "userId": 10,
    "username": "jane",
    "postId": 5,
    "createdAt": "2024-01-15T11:00:00"
  }
]
```

#### `POST /api/comments`
**Purpose**: Add a comment to a post

**Security**: Requires authentication

**Request Body**:
```json
{
  "postId": 5,
  "text": "This is my comment"
}
```

**Response** (200 OK): Created comment

#### `PUT /api/comments/{id}`
**Purpose**: Edit a comment

**Security**: Only comment author can edit

**Request Body**:
```json
{
  "text": "Updated comment text"
}
```

**Response** (200 OK): Updated comment

#### `DELETE /api/comments/{id}`
**Purpose**: Delete a comment

**Security**: Comment author or post author can delete

**Response** (204 No Content): Empty

---

### 4. LikeController.java
**Base URL**: `/api/likes`

**Purpose**: Like/unlike posts

**Endpoints**:

#### `POST /api/likes/post/{postId}`
**Purpose**: Like or unlike a post (toggle)

**Security**: Requires authentication

**URL Example**: `/api/likes/post/5`

**Response** (200 OK):
```json
{
  "liked": true,
  "message": "Post liked"
}
```

**If already liked**:
```json
{
  "liked": false,
  "message": "Post unliked"
}
```

#### `GET /api/likes/post/{postId}/check`
**Purpose**: Check if current user liked a post

**Security**: Requires authentication

**Response** (200 OK):
```json
{
  "liked": true
}
```

---

### 5. SubscriptionController.java
**Base URL**: `/api/subscriptions`

**Purpose**: Follow/unfollow users

**Endpoints**:

#### `POST /api/subscriptions/follow/{userId}`
**Purpose**: Follow a user

**Security**: Requires authentication

**URL Example**: `/api/subscriptions/follow/10`

**Response** (200 OK):
```json
{
  "message": "Successfully followed user"
}
```

**Errors**:
- 409 Conflict - Already following
- 400 Bad Request - Can't follow yourself

#### `POST /api/subscriptions/unfollow/{userId}`
**Purpose**: Unfollow a user

**Security**: Requires authentication

**Response** (200 OK):
```json
{
  "message": "Successfully unfollowed user"
}
```

#### `GET /api/subscriptions/followers/{userId}`
**Purpose**: Get list of users following this user

**Security**: Public

**Response** (200 OK):
```json
[
  {
    "id": 10,
    "username": "jane",
    "bio": "Hello!",
    "profilePicture": "/uploads/pic.jpg",
    "isFollowing": false
  }
]
```

#### `GET /api/subscriptions/following/{userId}`
**Purpose**: Get list of users this user follows

**Security**: Public

**Response** (200 OK): Same as followers

---

### 6. UserController.java
**Base URL**: `/api/users`

**Purpose**: User profile operations

**Endpoints**:

#### `GET /api/users/profile/{username}`
**Purpose**: Get user profile

**Security**: Public

**URL Example**: `/api/users/profile/john`

**Response** (200 OK):
```json
{
  "id": 5,
  "username": "john",
  "email": "john@example.com",
  "bio": "Software developer",
  "profilePicture": "/uploads/pic.jpg",
  "coverImage": "/uploads/cover.jpg",
  "postsCount": 15,
  "followersCount": 50,
  "followingCount": 30,
  "createdAt": "2024-01-01T00:00:00"
}
```

#### `PUT /api/users/profile`
**Purpose**: Update own profile

**Security**: Requires authentication

**Request Body**:
```json
{
  "bio": "Updated bio",
  "profilePicture": "/uploads/new-pic.jpg",
  "coverImage": "/uploads/new-cover.jpg"
}
```

**Response** (200 OK): Updated profile

#### `POST /api/users/profile-picture`
**Purpose**: Upload profile picture

**Security**: Requires authentication

**Content-Type**: `multipart/form-data`

**Request Body**: File upload (image)

**Response** (200 OK):
```json
{
  "message": "Profile picture uploaded successfully",
  "filePath": "/uploads/pic_12345.jpg"
}
```

**Code**:
```java
@PostMapping("/profile-picture")
public ResponseEntity<Map<String, String>> uploadProfilePicture(
    @RequestParam("file") MultipartFile file,
    @AuthenticationPrincipal UserDetails userDetails
) {
    Long userId = ((User) userDetails).getId();
    String filePath = userService.uploadProfilePicture(file, userId);
    
    return ResponseEntity.ok(Map.of(
        "message", "Profile picture uploaded successfully",
        "filePath", filePath
    ));
}
```

---

### 7. ReportController.java
**Base URL**: `/api/reports`

**Purpose**: Content reporting and admin management

**Endpoints**:

#### `POST /api/reports`
**Purpose**: Report content (post, comment, or user)

**Security**: Requires authentication

**Request Body**:
```json
{
  "reportType": "POST",
  "contentId": 5,
  "reason": "SPAM",
  "description": "This is spam content"
}
```

**Response** (200 OK):
```json
{
  "message": "Report submitted successfully"
}
```

**Errors**:
- 409 Conflict - Already reported this content
- 400 Bad Request - Can't report own content

#### `GET /api/reports` (Admin Only)
**Purpose**: Get all reports for admin dashboard

**Security**: Requires ADMIN role

**Response** (200 OK):
```json
[
  {
    "id": 1,
    "reportType": "POST",
    "reason": "SPAM",
    "description": "Promotional content",
    "status": "PENDING",
    "reporterUsername": "john",
    "contentId": 5,
    "contentPreview": "This is the post content...",
    "contentAuthor": "jane",
    "createdAt": "2024-01-15T10:00:00"
  }
]
```

**Code**:
```java
@GetMapping
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<List<ReportResponse>> getAllReports() {
    return ResponseEntity.ok(reportService.getAllReports());
}
```

**What is @PreAuthorize?**
- Checks role before executing method
- If not admin → 403 Forbidden
- Uses Spring Security

#### `GET /api/reports/status/{status}` (Admin Only)
**Purpose**: Filter reports by status

**Security**: Requires ADMIN role

**URL Example**: `/api/reports/status/PENDING`

**Response** (200 OK): Filtered list of reports

#### `PUT /api/reports/{id}/status` (Admin Only)
**Purpose**: Update report status

**Security**: Requires ADMIN role

**URL Example**: `/api/reports/5/status`

**Request Body**:
```json
{
  "status": "RESOLVED",
  "adminNotes": "User has been warned"
}
```

**Response** (200 OK): Updated report

#### `GET /api/reports/pending/count` (Admin Only)
**Purpose**: Get count of pending reports

**Security**: Requires ADMIN role

**Response** (200 OK):
```json
{
  "count": 5
}
```

**Used in**: Admin navbar badge showing pending reports

---

## 🔐 Authentication Flow in Controllers

### 1. Public Endpoints
No authentication needed:
```java
@GetMapping("/posts")
public ResponseEntity<List<PostResponse>> getAllPosts() {
    // Anyone can access
}
```

### 2. Authenticated Endpoints
Requires JWT token:
```java
@PostMapping("/posts")
public ResponseEntity<PostResponse> createPost(
    @RequestBody CreatePostRequest request,
    @AuthenticationPrincipal UserDetails userDetails  // ← Injected from JWT
) {
    Long userId = ((User) userDetails).getId();
    // ...
}
```

**How it works**:
1. Frontend sends: `Authorization: Bearer <token>`
2. `JwtAuthenticationFilter` intercepts request
3. Validates token
4. Extracts user from token
5. Injects into `@AuthenticationPrincipal`

### 3. Admin-Only Endpoints
Requires ADMIN role:
```java
@GetMapping("/reports")
@PreAuthorize("hasRole('ADMIN')")  // ← Role check
public ResponseEntity<List<ReportResponse>> getAllReports() {
    // Only admins can access
}
```

---

## 📊 HTTP Status Codes

### Success (2xx)
- **200 OK** - Request successful, returning data
- **201 Created** - Resource created successfully
- **204 No Content** - Request successful, no response body (delete)

### Client Error (4xx)
- **400 Bad Request** - Validation failed, malformed request
- **401 Unauthorized** - No token or invalid token
- **403 Forbidden** - Valid token but insufficient permissions
- **404 Not Found** - Resource doesn't exist
- **409 Conflict** - Duplicate entry, already exists

### Server Error (5xx)
- **500 Internal Server Error** - Unexpected server error

---

## 🎯 Controller Best Practices

### 1. Keep Controllers Thin
Controllers should only:
- Parse requests
- Call services
- Return responses

**DON'T** put business logic in controllers!

### 2. Use DTOs
Never accept/return entities directly:
```java
// Good
@PostMapping
public ResponseEntity<PostResponse> create(@RequestBody CreatePostRequest request)

// Bad
@PostMapping
public ResponseEntity<Post> create(@RequestBody Post post)
```

### 3. Validate Input
Always use `@Valid`:
```java
@PostMapping
public ResponseEntity<PostResponse> create(@Valid @RequestBody CreatePostRequest request)
```

### 4. Use Meaningful URLs
RESTful URL design:
- `GET /posts` - Get all
- `GET /posts/5` - Get one
- `POST /posts` - Create
- `PUT /posts/5` - Update
- `DELETE /posts/5` - Delete

### 5. Return Appropriate Status Codes
```java
// Created
return ResponseEntity.status(HttpStatus.CREATED).body(post);

// No content
return ResponseEntity.noContent().build();

// Not found
return ResponseEntity.notFound().build();
```

---

## 📚 Summary

Controllers are the **public interface** of your API. They:
1. Define HTTP endpoints
2. Handle requests and responses
3. Validate input
4. Enforce authentication and authorization
5. Return appropriate status codes

Every API call goes through a controller!
