# 🧪 Stage 2 Testing Guide - Likes & Comments

## Prerequisites
1. Start the application: `./mvnw spring-boot:run`
2. Have PostgreSQL running
3. Register 2-3 test users
4. Create some test posts

## 📋 Testing Checklist

### Part 1: Authentication (Get Tokens)

#### 1.1 Register User
```bash
POST http://localhost:8080/api/auth/register

Request Body:
{
  "username": "alice",
  "email": "alice@test.com",
  "password": "password123"
}

Response:
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "username": "alice"
}
```

**Save the accessToken** - you'll need it for all requests below!

#### 1.2 Register Second User
```bash
POST http://localhost:8080/api/auth/register

Request Body:
{
  "username": "bob",
  "email": "bob@test.com",
  "password": "password123"
}
```

### Part 2: Create Test Posts

#### 2.1 Create Post (as Alice)
```bash
POST http://localhost:8080/api/posts
Authorization: Bearer <alice_access_token>

Request Body:
{
  "title": "My First Post",
  "content": "This is my awesome post!",
  "category": "TECH"
}

Response:
{
  "id": 1,
  "title": "My First Post",
  "content": "This is my awesome post!",
  "category": "TECH",
  "author": "alice",
  "authorId": 1,
  "likeCount": 0,        ← Initially 0
  "commentCount": 0,     ← Initially 0
  "createdAt": "2025-01-18T10:00:00"
}
```

**Save the post ID** (e.g., 1) - you'll use it below!

---

## 🧪 Testing Likes

### Test 3.1: Like a Post
```bash
POST http://localhost:8080/api/posts/1/likes
Authorization: Bearer <bob_access_token>

Response: "Post liked successfully"
```

**Expected Behavior:**
- ✅ Creates Like record in database
- ✅ Bob liked Alice's post
- ✅ Returns 200 OK

### Test 3.2: Try to Like Again (Should Fail)
```bash
POST http://localhost:8080/api/posts/1/likes
Authorization: Bearer <bob_access_token>

Response: 
{
  "message": "You have already liked this post"
}
```

**Expected Behavior:**
- ❌ Throws IllegalStateException
- ❌ Cannot like same post twice
- ✅ Returns 400 Bad Request

### Test 3.3: Get Like Count
```bash
GET http://localhost:8080/api/posts/1/likes/count
Authorization: Bearer <bob_access_token>

Response: 1
```

**Expected Behavior:**
- ✅ Returns number of likes
- ✅ Should be 1 (Bob's like)

### Test 3.4: Get Users Who Liked
```bash
GET http://localhost:8080/api/posts/1/likes
Authorization: Bearer <bob_access_token>

Response:
[
  {
    "userId": 2,
    "username": "bob",
    "profilePicture": null,
    "likedAt": "2025-01-18T10:05:00"
  }
]
```

**Expected Behavior:**
- ✅ Returns list of LikeResponse objects
- ✅ Shows Bob liked the post

### Test 3.5: Check Like Status
```bash
GET http://localhost:8080/api/posts/1/likes/status
Authorization: Bearer <bob_access_token>

Response: true
```

**Expected Behavior:**
- ✅ Returns `true` (Bob liked it)

```bash
GET http://localhost:8080/api/posts/1/likes/status
Authorization: Bearer <alice_access_token>

Response: false
```

**Expected Behavior:**
- ✅ Returns `false` (Alice didn't like her own post)

### Test 3.6: Unlike a Post
```bash
DELETE http://localhost:8080/api/posts/1/likes
Authorization: Bearer <bob_access_token>

Response: "Post unliked successfully"
```

**Expected Behavior:**
- ✅ Deletes Like record
- ✅ Like count should be 0 now
- ✅ Returns 200 OK

### Test 3.7: Try to Unlike Again (Should Fail)
```bash
DELETE http://localhost:8080/api/posts/1/likes
Authorization: Bearer <bob_access_token>

Response:
{
  "message": "You have not liked this post"
}
```

**Expected Behavior:**
- ❌ Throws EntityNotFoundException
- ❌ Cannot unlike if not liked
- ✅ Returns 404 Not Found

---

## 💬 Testing Comments

### Test 4.1: Add Comment
```bash
POST http://localhost:8080/api/posts/1/comments
Authorization: Bearer <bob_access_token>

Request Body:
{
  "text": "Great post, Alice!"
}

Response:
{
  "id": 1,
  "text": "Great post, Alice!",
  "postId": 1,
  "authorId": 2,
  "authorUsername": "bob",
  "authorProfilePicture": null,
  "createdAt": "2025-01-18T10:10:00",
  "updatedAt": "2025-01-18T10:10:00",
  "isEdited": false,
  "isOwnComment": true
}
```

**Expected Behavior:**
- ✅ Creates Comment record
- ✅ Returns 201 CREATED
- ✅ `isEdited` is false (just created)
- ✅ `isOwnComment` is true (Bob is viewing)

### Test 4.2: Add Another Comment (as Alice)
```bash
POST http://localhost:8080/api/posts/1/comments
Authorization: Bearer <alice_access_token>

Request Body:
{
  "text": "Thanks Bob!"
}

Response:
{
  "id": 2,
  "text": "Thanks Bob!",
  "postId": 1,
  "authorId": 1,
  "authorUsername": "alice",
  "authorProfilePicture": null,
  "createdAt": "2025-01-18T10:11:00",
  "updatedAt": "2025-01-18T10:11:00",
  "isEdited": false,
  "isOwnComment": true
}
```

### Test 4.3: Get All Comments for Post
```bash
GET http://localhost:8080/api/posts/1/comments
Authorization: Bearer <bob_access_token>

Response:
[
  {
    "id": 2,
    "text": "Thanks Bob!",
    "authorUsername": "alice",
    "createdAt": "2025-01-18T10:11:00"
  },
  {
    "id": 1,
    "text": "Great post, Alice!",
    "authorUsername": "bob",
    "createdAt": "2025-01-18T10:10:00"
  }
]
```

**Expected Behavior:**
- ✅ Returns list sorted by newest first
- ✅ Alice's comment appears first (it's newer)

### Test 4.4: Get Comment Count
```bash
GET http://localhost:8080/api/posts/1/comments/count
Authorization: Bearer <bob_access_token>

Response: 2
```

**Expected Behavior:**
- ✅ Returns 2 (Bob's + Alice's comments)

### Test 4.5: Update Own Comment
```bash
PUT http://localhost:8080/api/posts/1/comments/1
Authorization: Bearer <bob_access_token>

Request Body:
{
  "text": "Great post, Alice! Updated!"
}

Response:
{
  "id": 1,
  "text": "Great post, Alice! Updated!",
  "postId": 1,
  "authorUsername": "bob",
  "createdAt": "2025-01-18T10:10:00",
  "updatedAt": "2025-01-18T10:15:00",    ← Updated time
  "isEdited": true,                       ← Now true!
  "isOwnComment": true
}
```

**Expected Behavior:**
- ✅ Updates comment text
- ✅ `updatedAt` changes to current time
- ✅ `isEdited` becomes `true`

### Test 4.6: Try to Update Someone Else's Comment (Should Fail)
```bash
PUT http://localhost:8080/api/posts/1/comments/2
Authorization: Bearer <bob_access_token>

Request Body:
{
  "text": "Trying to edit Alice's comment"
}

Response:
{
  "message": "You can only update your own comments"
}
```

**Expected Behavior:**
- ❌ Throws AccessDeniedException
- ❌ Bob cannot edit Alice's comment
- ✅ Returns 403 Forbidden

### Test 4.7: Delete Own Comment
```bash
DELETE http://localhost:8080/api/posts/1/comments/1
Authorization: Bearer <bob_access_token>

Response: "Comment deleted successfully"
```

**Expected Behavior:**
- ✅ Deletes Bob's comment
- ✅ Comment count is now 1

### Test 4.8: Admin Deletes Any Comment
**First, make Bob an admin:**
```sql
-- In your PostgreSQL database:
UPDATE users SET role = 'ADMIN' WHERE email = 'bob@test.com';
```

Then:
```bash
DELETE http://localhost:8080/api/posts/1/comments/2
Authorization: Bearer <bob_access_token>

Response: "Comment deleted successfully"
```

**Expected Behavior:**
- ✅ Admin (Bob) can delete Alice's comment
- ✅ Moderation capability works

### Test 4.9: Get Comments by User
```bash
GET http://localhost:8080/api/users/2/comments
Authorization: Bearer <bob_access_token>

Response:
[
  {
    "id": 3,
    "text": "Another comment by Bob",
    "postId": 2,
    "authorUsername": "bob",
    "createdAt": "2025-01-18T10:20:00"
  }
]
```

**Expected Behavior:**
- ✅ Returns all comments by Bob
- ✅ Useful for user profile page

---

## 🔍 Validation Tests

### Test 5.1: Empty Comment Text (Should Fail)
```bash
POST http://localhost:8080/api/posts/1/comments
Authorization: Bearer <bob_access_token>

Request Body:
{
  "text": ""
}

Response:
{
  "message": "text: Comment text cannot be blank"
}
```

**Expected Behavior:**
- ❌ Validation fails (@NotBlank)
- ✅ Returns 400 Bad Request

### Test 5.2: Missing Authorization (Should Fail)
```bash
POST http://localhost:8080/api/posts/1/likes

Response:
{
  "message": "Unauthorized"
}
```

**Expected Behavior:**
- ❌ No token provided
- ✅ Returns 401 Unauthorized

### Test 5.3: Like Non-Existent Post (Should Fail)
```bash
POST http://localhost:8080/api/posts/99999/likes
Authorization: Bearer <bob_access_token>

Response:
{
  "message": "Post not found with ID: 99999"
}
```

**Expected Behavior:**
- ❌ Throws EntityNotFoundException
- ✅ Returns 404 Not Found

---

## 📊 Database Verification

After testing, verify in PostgreSQL:

```sql
-- Check likes table
SELECT * FROM likes;

-- Check comments table
SELECT * FROM comments;

-- Count likes per post
SELECT post_id, COUNT(*) as like_count 
FROM likes 
GROUP BY post_id;

-- Count comments per post
SELECT post_id, COUNT(*) as comment_count 
FROM comments 
GROUP BY post_id;
```

---

## ✅ What to Verify

### For Likes:
- ✅ Like count increases/decreases correctly
- ✅ Cannot like same post twice
- ✅ Like status updates properly
- ✅ Unlike only works if previously liked

### For Comments:
- ✅ Comments appear in correct order (newest first)
- ✅ `isEdited` flag works correctly
- ✅ `isOwnComment` flag works correctly
- ✅ Only author can update comment
- ✅ Author or admin can delete
- ✅ Comment counts update properly
- ✅ `updatedAt` changes when edited

---

## 🐛 Troubleshooting

### Problem: 401 Unauthorized
**Solution:** Make sure you're sending the token:
```
Authorization: Bearer eyJhbGc...
```

### Problem: 403 Forbidden (Access Denied)
**Solution:** You're trying to edit/delete someone else's content

### Problem: 404 Not Found
**Solution:** Check that post ID or comment ID exists

### Problem: 400 Bad Request
**Solution:** Check your request body format and validation

---

## 📱 Using Postman

1. Create a **Collection** called "01Blog API"
2. Create an **Environment** with:
   - `baseUrl`: `http://localhost:8080`
   - `accessToken`: (paste your token here)
   - `postId`: (paste test post ID)

3. In request headers:
   ```
   Authorization: Bearer {{accessToken}}
   ```

4. In request URL:
   ```
   {{baseUrl}}/api/posts/{{postId}}/likes
   ```

This makes testing much easier!

---

## 🎉 Success Criteria

Stage 2 is complete when:
- [x] All like endpoints work
- [x] All comment endpoints work
- [x] Authorization rules enforced
- [x] Validation working
- [x] Database properly updated
- [x] No compilation errors

**Next:** Stage 3 - Reports System! 🚀
