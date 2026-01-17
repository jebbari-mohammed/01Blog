# Backend Models - Database Entities

## 📖 What are Models?

**Models** (also called **Entities**) are Java classes that represent **tables in your database**. Each model maps to one table, and each instance of a model represents one row in that table.

Think of it like this:
- **Model Class** = Blueprint for a table
- **Model Instance** = One row in that table
- **Model Fields** = Columns in that table

## 🔧 Key Annotations

### @Entity
Tells Spring/Hibernate: "This class is a database table"

### @Table(name = "table_name")
Specifies the exact table name in the database

### @Id
Marks a field as the **Primary Key** (unique identifier)

### @GeneratedValue
Auto-generates the ID value (usually auto-increment)

### @Column
Specifies column properties (name, length, nullable, etc.)

### @ManyToOne / @OneToMany / @ManyToMany
Define relationships between tables

### @Enumerated
Stores Java enums as strings in the database

### Lombok Annotations
- `@Data` - Generates getters, setters, toString, equals, hashCode
- `@Builder` - Allows builder pattern: `User.builder().username("john").build()`
- `@NoArgsConstructor` - Generates no-argument constructor
- `@AllArgsConstructor` - Generates constructor with all fields

---

## 📁 Models in this Project

### 1. User.java
**What it does**: Represents user accounts

**Table**: `_user` (underscore because "user" is a reserved word in PostgreSQL)

**Fields**:
- `id` - Unique identifier (auto-generated)
- `username` - User's display name (unique)
- `email` - User's email address (unique)
- `password` - Encrypted password (using BCrypt)
- `role` - USER or ADMIN
- `bio` - User's biography (optional)
- `profilePicture` - Path to profile image
- `coverImage` - Path to cover image
- `createdAt` - Account creation timestamp

**Relationships**:
- **One User → Many Posts**: User can create multiple posts
- **One User → Many Comments**: User can write multiple comments
- **One User → Many Likes**: User can like multiple posts
- **One User → Many Reports**: User can create multiple reports
- **Many-to-Many (Subscriptions)**: Users can follow each other

**Used in**: Authentication, user profiles, post creation

---

### 2. Post.java
**What it does**: Represents blog posts

**Table**: `posts`

**Fields**:
- `id` - Unique identifier
- `title` - Post title
- `content` - Post body text
- `category` - Post category (optional)
- `mediaUrl` - Path to attached image/video
- `createdAt` - Post creation timestamp
- `user` - Reference to the user who created it

**Relationships**:
- **Many Posts → One User**: Each post belongs to one user
- **One Post → Many Comments**: Post can have multiple comments
- **One Post → Many Likes**: Post can have multiple likes

**Used in**: Home feed, user profiles, post creation/editing

---

### 3. Comment.java
**What it does**: Represents comments on posts

**Table**: `comments`

**Fields**:
- `id` - Unique identifier
- `text` - Comment content
- `createdAt` - Comment creation timestamp
- `updatedAt` - Last update timestamp
- `user` - Reference to the user who wrote it
- `post` - Reference to the post it belongs to

**Relationships**:
- **Many Comments → One User**: Each comment belongs to one user
- **Many Comments → One Post**: Each comment belongs to one post

**Used in**: Post detail page, comment sections

---

### 4. Like.java
**What it does**: Represents user likes on posts

**Table**: `likes`

**Fields**:
- `id` - Unique identifier
- `createdAt` - Like timestamp
- `user` - Reference to the user who liked
- `post` - Reference to the liked post

**Unique Constraint**: One user can only like a post once (user_id + post_id unique)

**Relationships**:
- **Many Likes → One User**: Each like belongs to one user
- **Many Likes → One Post**: Each like belongs to one post

**Used in**: Like/unlike functionality, like counts

---

### 5. Subscription.java
**What it does**: Represents follow relationships between users

**Table**: `subscriptions`

**Fields**:
- `id` - Unique identifier
- `follower` - User who is following
- `following` - User being followed
- `createdAt` - Subscription timestamp

**Unique Constraint**: One user can only follow another user once

**Relationships**:
- **Many Subscriptions → One User (follower)**
- **Many Subscriptions → One User (following)**

**Used in**: Follow/unfollow functionality, followers/following lists

---

### 6. Report.java
**What it does**: Represents user-reported content (posts, comments, users)

**Table**: `reports`

**Fields**:
- `id` - Unique identifier
- `reportType` - POST, COMMENT, or USER
- `reason` - Why it was reported (SPAM, HARASSMENT, etc.)
- `description` - Additional details (optional)
- `status` - PENDING, REVIEWED, RESOLVED, DISMISSED
- `reporter` - User who created the report
- `post` - Reported post (if type is POST)
- `comment` - Reported comment (if type is COMMENT)
- `reportedUser` - Reported user (if type is USER)
- `reviewedBy` - Admin who reviewed it
- `adminNotes` - Admin's comments
- `createdAt` - Report creation timestamp
- `reviewedAt` - Review timestamp

**Validation**: Exactly one of post/comment/reportedUser must be set

**Used in**: Report functionality, admin dashboard

---

### 7. ReportType.java (Enum)
**What it does**: Defines types of content that can be reported

**Values**:
- `POST` - Report a blog post
- `COMMENT` - Report a comment
- `USER` - Report a user account

---

### 8. ReportReason.java (Enum)
**What it does**: Defines reasons for reporting content

**Values**:
- `SPAM` - Unwanted promotional content
- `HARASSMENT` - Bullying or harassment
- `HATE_SPEECH` - Hate speech or discrimination
- `VIOLENCE` - Violent or graphic content
- `SEXUAL_CONTENT` - Inappropriate sexual content
- `FALSE_INFORMATION` - Misinformation
- `COPYRIGHT` - Copyright violation
- `OTHER` - Other reasons

---

### 9. ReportStatus.java (Enum)
**What it does**: Defines report lifecycle states

**Values**:
- `PENDING` - Just submitted, awaiting review
- `REVIEWED` - Admin has seen it
- `RESOLVED` - Action taken (content removed, user banned, etc.)
- `DISMISSED` - No action needed

---

### 10. Role.java (Enum)
**What it does**: Defines user roles

**Values**:
- `USER` - Regular user (default)
- `ADMIN` - Administrator with special permissions

---

## 🔗 Relationship Types Explained

### @ManyToOne
**"Many of this → One of that"**

Example: Many Posts → One User
```java
@ManyToOne
@JoinColumn(name = "user_id")
private User user;
```
**Database**: Creates a `user_id` column in the `posts` table

### @OneToMany
**"One of this → Many of that"**

Example: One User → Many Posts
```java
@OneToMany(mappedBy = "user")
private List<Post> posts;
```
**Note**: `mappedBy` means this side doesn't own the relationship

### @ManyToMany
**"Many of this ↔ Many of that"**

Example: Users following each other
- Requires a **join table** (subscriptions)
- Join table has two foreign keys (follower_id, following_id)

---

## 💾 How Models Work with Database

### 1. Hibernate ORM
Hibernate is the **ORM** (Object-Relational Mapping) tool that:
- Translates Java objects to SQL
- Automatically creates/updates tables
- Manages relationships

### 2. DDL Auto Generation
In `application.properties`:
```properties
spring.jpa.hibernate.ddl-auto=update
```

**Options**:
- `create` - Drop and recreate tables every time
- `update` - Update tables without losing data
- `validate` - Only check if schema matches
- `none` - Do nothing

### 3. Column Naming
- Java: `createdAt` (camelCase)
- Database: `created_at` (snake_case)
- Hibernate auto-converts!

---

## 🎯 Best Practices

### 1. Use @Builder
Makes object creation clean:
```java
User user = User.builder()
    .username("john")
    .email("john@example.com")
    .password("encrypted")
    .role(Role.USER)
    .build();
```

### 2. Use Lombok
Reduces boilerplate code significantly

### 3. Lazy Loading
```java
@ManyToOne(fetch = FetchType.LAZY)
```
Doesn't load related entities until accessed (better performance)

### 4. Cascading
```java
@OneToMany(cascade = CascadeType.ALL)
```
Operations on parent affect children (e.g., delete user → delete their posts)

### 5. Validation
Use `@Column(nullable = false)` for required fields

---

## 🔍 Common Patterns

### Timestamps
```java
@CreationTimestamp
private LocalDateTime createdAt;

@UpdateTimestamp
private LocalDateTime updatedAt;
```

### Unique Constraints
```java
@Column(unique = true)
private String email;
```

### Enums
```java
@Enumerated(EnumType.STRING)
private Role role;
```
**STRING** stores "USER" or "ADMIN" (readable)
**ORDINAL** stores 0 or 1 (fragile if enum order changes)

---

## 📚 Summary

Models are the **foundation** of your application. They:
1. Define your database structure
2. Represent business entities
3. Handle relationships between data
4. Ensure data integrity with constraints

Every feature in your app starts with a model!
