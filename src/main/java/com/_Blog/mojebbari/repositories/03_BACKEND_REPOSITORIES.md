# Backend Repositories - Database Access Layer

## 📖 What are Repositories?

**Repositories** are interfaces that provide methods to interact with the database. Think of them as a **bridge between your Java code and the database**.

Instead of writing SQL queries manually, repositories give you ready-made methods like:
- `save()` - Insert or update
- `findById()` - Get by ID
- `findAll()` - Get all records
- `deleteById()` - Delete by ID

## 🔧 Key Concepts

### JpaRepository<Entity, ID>
- **Entity**: The model class (e.g., User, Post)
- **ID**: The type of the primary key (usually Long)

Example:
```java
public interface UserRepository extends JpaRepository<User, Long> {
    // User is the entity
    // Long is the type of the ID field
}
```

### Automatic Methods
When you extend `JpaRepository`, you automatically get:
- `save(T entity)` - Save or update
- `findById(ID id)` - Find by primary key
- `findAll()` - Get all records
- `deleteById(ID id)` - Delete by ID
- `count()` - Count total records
- `existsById(ID id)` - Check if exists

### Custom Query Methods
Spring Data JPA can create queries from method names!

**Convention**: `findBy + FieldName + Condition`

Examples:
- `findByUsername(String username)` → `SELECT * FROM user WHERE username = ?`
- `findByEmail(String email)` → `SELECT * FROM user WHERE email = ?`
- `existsByUsername(String username)` → Check if username exists
- `countByStatus(Status status)` → Count records with specific status

### @Query Annotation
For complex queries, write custom JPQL (Java Persistence Query Language):
```java
@Query("SELECT u FROM User u WHERE u.email = :email OR u.username = :username")
Optional<User> findByEmailOrUsername(String email, String username);
```

---

## 📁 Repositories in this Project

### 1. UserRepository.java
**Purpose**: Manage user accounts

**Custom Methods**:
```java
// Find user by email OR username (for login)
Optional<User> findByEmailOrUsername(String email, String username);

// Find by email only
Optional<User> findByEmail(String email);

// Find by username only
Optional<User> findByUsername(String username);

// Check if username exists (for registration validation)
boolean existsByUsername(String username);

// Check if email exists (for registration validation)
boolean existsByEmail(String email);
```

**Why `Optional<User>`?**
- `Optional` means the result might be null (user not found)
- Prevents `NullPointerException`
- You must check: `if (userOptional.isPresent()) { ... }`

**Used in**:
- `AuthService` - Login, registration
- `UserService` - Get user profiles
- `SecurityConfig` - Load user for authentication

**Example Usage**:
```java
// Check if username is taken
if (userRepository.existsByUsername("john")) {
    throw new IllegalStateException("Username already taken!");
}

// Find user for login
Optional<User> userOpt = userRepository.findByEmailOrUsername("john@example.com", "john");
if (userOpt.isPresent()) {
    User user = userOpt.get();
    // Process user
}
```

---

### 2. PostRepository.java
**Purpose**: Manage blog posts

**Custom Methods**:
```java
// Get all posts ordered by newest first
List<Post> findAllByOrderByCreatedAtDesc();

// Get all posts by a specific user
List<Post> findByUserIdOrderByCreatedAtDesc(Long userId);

// Get posts by category
List<Post> findByCategoryOrderByCreatedAtDesc(String category);
```

**Naming Convention Explained**:
- `findBy` - SELECT query
- `UserId` - Filter by user.id field
- `OrderBy` - Sort the results
- `CreatedAtDesc` - Sort by createdAt descending (newest first)

**Used in**:
- `PostService` - Get posts for home feed
- `UserController` - Get user's posts for profile page

**Example Usage**:
```java
// Get all posts (home feed)
List<Post> allPosts = postRepository.findAllByOrderByCreatedAtDesc();

// Get user's posts (profile page)
List<Post> userPosts = postRepository.findByUserIdOrderByCreatedAtDesc(userId);
```

---

### 3. CommentRepository.java
**Purpose**: Manage comments on posts

**Custom Methods**:
```java
// Get all comments for a specific post
List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);
```

**Why `Asc` (Ascending)?**
- Comments display oldest → newest (conversation flow)
- Unlike posts which show newest → oldest

**Used in**:
- `CommentService` - Get comments for a post

**Example Usage**:
```java
// Get all comments for post ID 5
List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(5L);
```

---

### 4. LikeRepository.java
**Purpose**: Manage likes on posts

**Custom Methods**:
```java
// Check if a user already liked a post
@Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END " +
       "FROM Like l WHERE l.user.id = :userId AND l.post.id = :postId")
boolean existsByUserIdAndPostId(Long userId, Long postId);

// Find a specific like (to delete it for unlike)
Optional<Like> findByUserIdAndPostId(Long userId, Long postId);

// Count total likes on a post
long countByPostId(Long postId);
```

**JPQL Explained**:
- `SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END` - Returns boolean
- `FROM Like l` - Query the Like entity (alias 'l')
- `WHERE l.user.id = :userId AND l.post.id = :postId` - Filter conditions
- `:userId` and `:postId` are method parameters

**Used in**:
- `LikeService` - Like/unlike functionality
- `PostService` - Get like counts for posts

**Example Usage**:
```java
// Check if user 3 liked post 10
boolean hasLiked = likeRepository.existsByUserIdAndPostId(3L, 10L);

// Unlike: find and delete
Optional<Like> like = likeRepository.findByUserIdAndPostId(3L, 10L);
like.ifPresent(l -> likeRepository.delete(l));

// Count likes
long likeCount = likeRepository.countByPostId(10L);
```

---

### 5. SubscriptionRepository.java
**Purpose**: Manage follow relationships between users

**Custom Methods**:
```java
// Check if user A follows user B
@Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
       "FROM Subscription s WHERE s.follower.id = :followerId " +
       "AND s.following.id = :followingId")
boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

// Find subscription (to delete for unfollow)
Optional<Subscription> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

// Get all followers of a user
List<Subscription> findByFollowingIdOrderByCreatedAtDesc(Long followingId);

// Get all users that a user follows
List<Subscription> findByFollowerIdOrderByCreatedAtDesc(Long followerId);

// Count followers
long countByFollowingId(Long userId);

// Count following
long countByFollowerId(Long userId);
```

**Naming Explained**:
- `Follower` - The user who is following
- `Following` - The user being followed
- Example: John follows Jane
  - John is the **follower**
  - Jane is the **following**

**Used in**:
- `SubscriptionService` - Follow/unfollow functionality
- `UserController` - Get followers/following lists

**Example Usage**:
```java
// Check if user 5 follows user 10
boolean isFollowing = subscriptionRepository.existsByFollowerIdAndFollowingId(5L, 10L);

// Unfollow
Optional<Subscription> sub = subscriptionRepository.findByFollowerIdAndFollowingId(5L, 10L);
sub.ifPresent(s -> subscriptionRepository.delete(s));

// Get all followers of user 10
List<Subscription> followers = subscriptionRepository.findByFollowingIdOrderByCreatedAtDesc(10L);

// Count followers
long followerCount = subscriptionRepository.countByFollowingId(10L);
```

---

### 6. ReportRepository.java
**Purpose**: Manage user reports

**Custom Methods**:
```java
// Get all reports by status
List<Report> findByStatusOrderByCreatedAtDesc(ReportStatus status);

// Get all reports by type (POST, COMMENT, USER)
List<Report> findByReportTypeOrderByCreatedAtDesc(ReportType reportType);

// Get all reports ordered by newest
List<Report> findAllByOrderByCreatedAtDesc();

// Check if user already reported a post
@Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
       "FROM Report r WHERE r.reporter.id = :reporterId AND r.post.id = :postId")
boolean existsByReporterIdAndPostId(Long reporterId, Long postId);

// Check if user already reported a comment
@Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
       "FROM Report r WHERE r.reporter.id = :reporterId AND r.comment.id = :commentId")
boolean existsByReporterIdAndCommentId(Long reporterId, Long commentId);

// Check if user already reported another user
@Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
       "FROM Report r WHERE r.reporter.id = :reporterId " +
       "AND r.reportedUser.id = :reportedUserId")
boolean existsByReporterIdAndReportedUserId(Long reporterId, Long reportedUserId);

// Get all reports for a specific post
List<Report> findByPostIdOrderByCreatedAtDesc(Long postId);

// Get all reports for a specific comment
List<Report> findByCommentIdOrderByCreatedAtDesc(Long commentId);

// Get all reports for a specific user
List<Report> findByReportedUserIdOrderByCreatedAtDesc(Long userId);

// Count pending reports
long countByStatus(ReportStatus status);
```

**Used in**:
- `ReportService` - Create reports, get reports for admin
- `AdminController` - Admin dashboard

**Example Usage**:
```java
// Check if user already reported this post (prevent duplicates)
if (reportRepository.existsByReporterIdAndPostId(userId, postId)) {
    throw new IllegalStateException("You already reported this post");
}

// Get all pending reports
List<Report> pending = reportRepository.findByStatusOrderByCreatedAtDesc(ReportStatus.PENDING);

// Count pending reports (for admin badge)
long count = reportRepository.countByStatus(ReportStatus.PENDING);
```

---

## 🔍 Understanding Optional<T>

### What is Optional?
A container that may or may not contain a value. It prevents null pointer exceptions.

### Methods:
```java
Optional<User> userOpt = userRepository.findById(1L);

// Check if present
if (userOpt.isPresent()) {
    User user = userOpt.get();
}

// Or throw exception if not found
User user = userOpt.orElseThrow(() -> new EntityNotFoundException("User not found"));

// Or provide default value
User user = userOpt.orElse(defaultUser);

// Or execute action if present
userOpt.ifPresent(user -> System.out.println(user.getUsername()));
```

---

## 🎯 Best Practices

### 1. Use Method Name Queries When Possible
```java
// Simple queries - Spring generates SQL
List<Post> findByUserId(Long userId);

// Complex queries - Write JPQL
@Query("SELECT p FROM Post p WHERE p.user.id = :userId AND p.createdAt > :date")
List<Post> findRecentPostsByUser(Long userId, LocalDateTime date);
```

### 2. Always Use Optional for findBy Methods
```java
// Good
Optional<User> findByUsername(String username);

// Bad (can return null)
User findByUsername(String username);
```

### 3. Use Descriptive Method Names
```java
// Good
List<Post> findByUserIdOrderByCreatedAtDesc(Long userId);

// Bad
List<Post> getUserPosts(Long userId); // Spring won't generate query
```

### 4. Use exists Methods for Checks
```java
// Efficient - only checks existence
boolean existsByUsername(String username);

// Inefficient - loads entire object
Optional<User> user = findByUsername(username);
boolean exists = user.isPresent();
```

---

## 📊 Query Method Keywords

### Find/Read/Get
All equivalent: `findByName`, `readByName`, `getByName`

### Conditions
- `findByAgeGreaterThan(int age)`
- `findByAgeLessThan(int age)`
- `findByAgeGreaterThanEqual(int age)`
- `findByAgeBetween(int start, int end)`
- `findByNameLike(String pattern)` - SQL LIKE
- `findByNameContaining(String text)` - Contains substring
- `findByNameStartingWith(String prefix)`
- `findByNameEndingWith(String suffix)`

### Combining
- `findByNameAndAge(String name, int age)` - AND
- `findByNameOrAge(String name, int age)` - OR
- `findByAgeOrderByNameAsc(int age)` - Sort ascending
- `findByAgeOrderByNameDesc(int age)` - Sort descending

---

## 📚 Summary

Repositories are your **data access layer**. They:
1. Provide CRUD operations automatically
2. Generate queries from method names
3. Support custom JPQL queries
4. Handle database transactions
5. Return Optional for null safety

Every service in your app uses repositories to interact with the database!
