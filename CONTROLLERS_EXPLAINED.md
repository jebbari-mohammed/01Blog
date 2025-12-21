# 📖 LikeController & CommentController - Line by Line Explanation

## 🎯 Overview

These controllers expose REST APIs for users to:
- **Like/Unlike** posts
- **Comment** on posts
- **Update/Delete** their own comments
- **View** likes and comments

---

## 📝 LikeController.java

### Class Declaration

```java
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class LikeController {
```

**Line-by-line:**

1. **`@RestController`**
   - Combines `@Controller` + `@ResponseBody`
   - Makes this class handle HTTP requests
   - Automatically converts return values to JSON

2. **`@RequestMapping("/api/posts")`**
   - Base URL for all endpoints in this controller
   - All methods inherit this prefix
   - Example: Method with `/{postId}/likes` becomes `/api/posts/{postId}/likes`

3. **`@RequiredArgsConstructor`**
   - Lombok annotation
   - Generates constructor for all `final` fields
   - Spring injects dependencies automatically

---

### Dependency Injection

```java
private final LikeService likeService;
```

**Explanation:**
- `final` means it must be set in constructor
- Lombok's `@RequiredArgsConstructor` creates the constructor
- Spring automatically injects `LikeService` instance
- This is **constructor injection** (best practice)

**Without Lombok, it would be:**
```java
public LikeController(LikeService likeService) {
    this.likeService = likeService;
}
```

---

### Method 1: Like a Post

```java
@PostMapping("/{postId}/likes")
public ResponseEntity<String> likePost(
        @PathVariable Long postId,
        Authentication authentication) {
    
    String userEmail = authentication.getName();
    likeService.likePost(postId, userEmail);
    return ResponseEntity.ok("Post liked successfully");
}
```

**Line-by-line:**

1. **`@PostMapping("/{postId}/likes")`**
   - Handles **POST** requests (create resource)
   - `{postId}` is a **path variable** (dynamic part of URL)
   - Full URL: `POST /api/posts/123/likes`
   - Used to create a new "like" record

2. **`ResponseEntity<String>`**
   - Return type that gives us full control over HTTP response
   - `<String>` means response body is a String
   - We can set status codes (200, 404, etc.)

3. **`@PathVariable Long postId`**
   - Extracts `{postId}` from URL and converts to Long
   - Example: `/api/posts/123/likes` → `postId = 123`
   - Spring does the conversion automatically

4. **`Authentication authentication`**
   - Provided by **Spring Security**
   - Contains info about the currently logged-in user
   - Automatically injected by Spring

5. **`authentication.getName()`**
   - Returns the "username" from JWT token
   - In our case, "username" is the **email**
   - This is how we know who is liking the post

6. **`likeService.likePost(postId, userEmail)`**
   - Calls service layer to do the actual work
   - Service handles business logic and database
   - Controller just coordinates

7. **`ResponseEntity.ok(...)`**
   - Creates HTTP 200 (OK) response
   - Body: "Post liked successfully"
   - Equivalent to: `ResponseEntity.status(200).body("...")`

---

### Method 2: Unlike a Post

```java
@DeleteMapping("/{postId}/likes")
public ResponseEntity<String> unlikePost(
        @PathVariable Long postId,
        Authentication authentication) {
    
    String userEmail = authentication.getName();
    likeService.unlikePost(postId, userEmail);
    return ResponseEntity.ok("Post unliked successfully");
}
```

**Key Differences from POST:**

1. **`@DeleteMapping`**
   - Handles **DELETE** requests (remove resource)
   - RESTful convention: DELETE to remove
   - Same URL as POST, different HTTP method

2. **Why same URL?**
   - REST principle: URL represents a **resource**
   - `/api/posts/123/likes` = "your like on post 123"
   - POST = create it, DELETE = remove it
   - Same resource, different actions

---

### Method 3: Get List of Likes

```java
@GetMapping("/{postId}/likes")
public ResponseEntity<List<LikeResponse>> getLikes(@PathVariable Long postId) {
    List<LikeResponse> likes = likeService.getLikesByPost(postId);
    return ResponseEntity.ok(likes);
}
```

**Line-by-line:**

1. **`@GetMapping`**
   - Handles **GET** requests (read data)
   - Used to retrieve information
   - No request body needed

2. **`ResponseEntity<List<LikeResponse>>`**
   - Returns a list of LikeResponse DTOs
   - Spring automatically converts to JSON array
   - Example: `[{userId: 1, username: "alice"}, {userId: 2, username: "bob"}]`

3. **No `Authentication` parameter**
   - This endpoint can be public
   - Anyone can see who liked a post
   - No login required (in this design)

---

### Method 4: Check Like Status

```java
@GetMapping("/{postId}/likes/status")
public ResponseEntity<Boolean> isLiked(
        @PathVariable Long postId,
        Authentication authentication) {
    
    String userEmail = authentication.getName();
    boolean isLiked = likeService.isPostLikedByUser(postId, userEmail);
    return ResponseEntity.ok(isLiked);
}
```

**Why is this useful?**

**Frontend Logic:**
```javascript
// When rendering a post, check if user liked it
const response = await fetch('/api/posts/123/likes/status');
const isLiked = await response.json(); // true or false

if (isLiked) {
    showFilledHeart(); // ❤️ Red heart
} else {
    showEmptyHeart();  // 🤍 Empty heart
}
```

**Key Points:**
1. Returns `Boolean` (true/false)
2. Requires authentication (uses current user's email)
3. Used to show UI state (liked vs not liked)

---

### Method 5: Get Like Count

```java
@GetMapping("/{postId}/likes/count")
public ResponseEntity<Long> getLikeCount(@PathVariable Long postId) {
    Long count = likeService.getLikeCount(postId);
    return ResponseEntity.ok(count);
}
```

**Usage:**
- Display "42 people liked this post"
- Public endpoint (no authentication)
- Returns just a number

---

## 💬 CommentController.java

### Class Declaration

```java
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
```

**Same structure as LikeController** - standard Spring Boot pattern.

---

### Method 1: Add Comment

```java
@PostMapping("/{postId}/comments")
public ResponseEntity<CommentResponse> addComment(
        @PathVariable Long postId,
        @Valid @RequestBody CommentRequest request,
        Authentication authentication) {
    
    String userEmail = authentication.getName();
    CommentResponse response = commentService.addComment(postId, userEmail, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

**New Concepts:**

1. **`@Valid`**
   - Triggers **validation** on the request body
   - Checks `@NotBlank`, `@Size`, etc. in `CommentRequest`
   - If validation fails, returns 400 Bad Request automatically

2. **`@RequestBody CommentRequest request`**
   - Reads JSON from request body
   - Converts it to `CommentRequest` object
   - Example JSON: `{"text": "Great post!"}`
   - Spring uses Jackson library for conversion

3. **`HttpStatus.CREATED`**
   - HTTP 201 status code
   - Means "resource successfully created"
   - RESTful best practice for POST requests

**Request Flow:**
```
1. POST /api/posts/123/comments
   Body: {"text": "Nice!"}

2. @Valid checks: Is text blank? → No ✓

3. Spring Security checks: Is user logged in? → Yes ✓

4. Controller extracts email: authentication.getName()

5. Service creates comment in database

6. Controller returns 201 CREATED with CommentResponse
```

---

### Method 2: Get All Comments

```java
@GetMapping("/{postId}/comments")
public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long postId) {
    List<CommentResponse> comments = commentService.getCommentsByPost(postId);
    return ResponseEntity.ok(comments);
}
```

**Simple GET endpoint:**
- No authentication needed (public)
- Returns list of comments
- Sorted by newest first (in service layer)

**Response Example:**
```json
[
  {
    "id": 5,
    "text": "Updated comment!",
    "authorUsername": "bob",
    "isEdited": true,
    "createdAt": "2025-01-18T10:00:00"
  },
  {
    "id": 4,
    "text": "First!",
    "authorUsername": "alice",
    "isEdited": false,
    "createdAt": "2025-01-18T09:55:00"
  }
]
```

---

### Method 3: Update Comment

```java
@PutMapping("/{postId}/comments/{commentId}")
public ResponseEntity<CommentResponse> updateComment(
        @PathVariable Long commentId,
        @Valid @RequestBody CommentRequest request,
        Authentication authentication) {
    
    String userEmail = authentication.getName();
    CommentResponse response = commentService.updateComment(commentId, userEmail, request);
    return ResponseEntity.ok(response);
}
```

**Key Points:**

1. **Two Path Variables:**
   - `{postId}` - Which post (not actually used in method, but nice for URL clarity)
   - `{commentId}` - Which comment to update
   - Example: `PUT /api/posts/123/comments/5`

2. **`@PutMapping`**
   - HTTP PUT method
   - Used for **updating** existing resources
   - RESTful convention

3. **Authorization happens in service:**
   - Service checks if email matches comment author
   - If not, throws `AccessDeniedException`
   - Controller doesn't need to check

---

### Method 4: Delete Comment

```java
@DeleteMapping("/{postId}/comments/{commentId}")
public ResponseEntity<String> deleteComment(
        @PathVariable Long commentId,
        Authentication authentication) {
    
    String userEmail = authentication.getName();
    commentService.deleteComment(commentId, userEmail);
    return ResponseEntity.ok("Comment deleted successfully");
}
```

**Authorization Rules (in service):**
```java
boolean isAuthor = comment.getUser().getEmail().equals(userEmail);
boolean isAdmin = currentUser.getRole() == Role.ADMIN;

if (!isAuthor && !isAdmin) {
    throw new AccessDeniedException("You can only delete your own comments");
}
```

**Possible Outcomes:**
- ✅ Author deletes own comment → Success
- ✅ Admin deletes any comment → Success (moderation)
- ❌ Other user tries to delete → 403 Forbidden

---

### Method 5: Get User's Comments

```java
@GetMapping("/users/{userId}/comments")
public ResponseEntity<List<CommentResponse>> getCommentsByUser(@PathVariable Long userId) {
    List<CommentResponse> comments = commentService.getCommentsByUser(userId);
    return ResponseEntity.ok(comments);
}
```

**Use Case:**
- User profile page: "View all comments by Alice"
- Shows comment history

**URL Pattern:**
- `/api/posts/users/5/comments`
- Notice: Still under `/api/posts` base path
- Different from other endpoints (uses `/users/`)

---

### Method 6: Get Comment Count

```java
@GetMapping("/{postId}/comments/count")
public ResponseEntity<Long> getCommentCount(@PathVariable Long postId) {
    Long count = commentService.getCommentCount(postId);
    return ResponseEntity.ok(count);
}
```

**Usage:**
- Display "15 comments" under a post
- Simple counter

---

## 🔐 Security Flow

### How Authentication Works:

```
1. User logs in:
   POST /api/auth/login
   → Receives JWT token

2. User makes request:
   POST /api/posts/123/likes
   Header: Authorization: Bearer eyJhbGc...

3. JwtAuthenticationFilter intercepts:
   → Validates token
   → Extracts email from token
   → Creates Authentication object
   → Stores in SecurityContext

4. Spring injects Authentication:
   → Controller receives it as parameter
   → No manual checking needed

5. Controller uses it:
   authentication.getName() → email
```

---

## 📊 HTTP Status Codes Used

| Code | Meaning | When Used |
|------|---------|-----------|
| 200 OK | Success | GET, PUT, DELETE successful |
| 201 CREATED | Resource created | POST comment/like successful |
| 400 BAD REQUEST | Invalid input | Validation fails (@Valid) |
| 401 UNAUTHORIZED | Not logged in | No JWT token |
| 403 FORBIDDEN | No permission | Edit someone else's comment |
| 404 NOT FOUND | Resource missing | Post/Comment doesn't exist |

---

## 🎨 REST API Design Principles

### 1. Resource-Based URLs
```
❌ Bad:  /api/likePost?id=123
✅ Good: /api/posts/123/likes
```

### 2. HTTP Methods = Actions
```
GET    → Read
POST   → Create
PUT    → Update (full replacement)
DELETE → Remove
```

### 3. Plural Nouns
```
❌ Bad:  /api/post/123
✅ Good: /api/posts/123
```

### 4. Nested Resources
```
/api/posts/{postId}/likes      → Likes belong to posts
/api/posts/{postId}/comments   → Comments belong to posts
```

### 5. Use HTTP Status Codes
```
Don't return 200 with {"error": "..."} in body
Return 404, 403, 400, etc. properly
```

---

## 🧪 Testing with Curl

### Like a post:
```bash
curl -X POST http://localhost:8080/api/posts/1/likes \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Add comment:
```bash
curl -X POST http://localhost:8080/api/posts/1/comments \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"text": "Great post!"}'
```

### Update comment:
```bash
curl -X PUT http://localhost:8080/api/posts/1/comments/5 \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"text": "Updated text!"}'
```

### Get likes:
```bash
curl http://localhost:8080/api/posts/1/likes
```

---

## 📱 Frontend Integration Example

### React Component Using These APIs:

```javascript
const PostCard = ({ post }) => {
  const [isLiked, setIsLiked] = useState(false);
  const [likeCount, setLikeCount] = useState(0);
  const [comments, setComments] = useState([]);

  useEffect(() => {
    // Check if user liked this post
    fetch(`/api/posts/${post.id}/likes/status`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    .then(res => res.json())
    .then(setIsLiked);

    // Get like count
    fetch(`/api/posts/${post.id}/likes/count`)
      .then(res => res.json())
      .then(setLikeCount);

    // Get comments
    fetch(`/api/posts/${post.id}/comments`)
      .then(res => res.json())
      .then(setComments);
  }, [post.id]);

  const handleLike = async () => {
    if (isLiked) {
      await fetch(`/api/posts/${post.id}/likes`, {
        method: 'DELETE',
        headers: { 'Authorization': `Bearer ${token}` }
      });
    } else {
      await fetch(`/api/posts/${post.id}/likes`, {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}` }
      });
    }
    setIsLiked(!isLiked);
    setLikeCount(isLiked ? likeCount - 1 : likeCount + 1);
  };

  const handleComment = async (text) => {
    await fetch(`/api/posts/${post.id}/comments`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ text })
    });
    // Refresh comments
  };

  return (
    <div>
      <h2>{post.title}</h2>
      <p>{post.content}</p>
      
      <button onClick={handleLike}>
        {isLiked ? '❤️' : '🤍'} {likeCount}
      </button>

      <div>
        {comments.map(comment => (
          <Comment key={comment.id} comment={comment} />
        ))}
      </div>
    </div>
  );
};
```

---

## 🎓 Key Takeaways

1. **Controllers are thin:**
   - Extract parameters
   - Call service
   - Return response
   - No business logic!

2. **Service layer does the work:**
   - Business rules
   - Authorization checks
   - Database operations

3. **Spring handles a lot:**
   - JSON conversion (Jackson)
   - Validation (@Valid)
   - Authentication (Spring Security)
   - Dependency injection

4. **REST is about resources:**
   - URLs represent resources
   - HTTP methods represent actions
   - Status codes indicate results

5. **Security is automatic:**
   - JWT filter runs before controller
   - Authentication object is ready to use
   - No manual token parsing needed

---

**Next:** Run the tests and move to Stage 3! 🚀
