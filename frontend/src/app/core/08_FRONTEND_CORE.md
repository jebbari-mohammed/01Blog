# Frontend Core Module - Services, Models, Guards & Interceptors

> **Status:** ✅ Complete | **Last Updated:** January 17, 2026

## 📖 What is the Core Module?

The **Core module** contains the foundation of the Angular application:
- **Services**: API communication and business logic (8 services)
- **Models**: TypeScript interfaces for type safety (10+ models)
- **Guards**: Route protection (auth.guard, admin.guard)
- **Interceptors**: HTTP request/response handling (JWT injection)

Think of Core as the **backbone** of your Angular app.

---

## ✅ Implemented Core Features

### Services (8 Total)
1. **AuthService** - Authentication, JWT management, user state
2. **PostService** - Post CRUD operations, including `getPostById()`
3. **CommentService** - Comment operations
4. **LikeService** - Like/unlike functionality
5. **SubscriptionService** - Follow/unfollow operations
6. **UserService** - User profile management
7. **ReportService** - Report submission and management
8. **NotificationService** - Notifications with auto-polling (30s)

### Models (10+ Interfaces)
- User, Post, Comment, Like, Subscription
- Notification, NotificationType enum
- AuthResponse, LoginRequest, RegisterRequest
- UserProfile, ReportRequest

### Guards (2)
- **AuthGuard** - Protects authenticated routes
- **AdminGuard** - Protects admin-only routes

### Interceptors (1)
- **AuthInterceptor** - Auto-injects JWT tokens into requests

---

## 🔧 Key Angular Concepts

### 1. Service (@Injectable)
A service is a class that contains reusable logic, typically API calls.

```typescript
@Injectable({
  providedIn: 'root'  // Single instance shared across app
})
export class PostService {
  constructor(private http: HttpClient) {}
  
  getPosts(): Observable<Post[]> {
    return this.http.get<Post[]>('http://localhost:8080/api/posts');
  }
}
```

**Why Services?**
- Reusable: Multiple components can use same service
- Testable: Easy to mock for testing
- Separation: Keep API logic separate from UI logic

### 2. Observable (RxJS)
Angular uses **Observables** for asynchronous operations.

```typescript
// Service returns Observable
getPosts(): Observable<Post[]> {
  return this.http.get<Post[]>('/api/posts');
}

// Component subscribes to get data
this.postService.getPosts().subscribe({
  next: (posts) => {
    this.posts = posts;  // Success
  },
  error: (error) => {
    console.error(error);  // Error
  }
});
```

**Think of Observable as a stream**:
- Service: "I'll call API and stream the result"
- Component: "I'll subscribe and handle the result"

### 3. Interface (TypeScript)
Defines the shape of data:

```typescript
export interface Post {
  id: number;
  title: string;
  content: string;
  authorId: number;
  createdAt: Date;
}
```

**Why Interfaces?**
- Type safety: TypeScript catches errors at compile time
- Autocomplete: IDE suggests available properties
- Documentation: Clear contract of what data looks like

### 4. HttpClient
Angular's service for making HTTP requests:

```typescript
// GET request
this.http.get<Post[]>('/api/posts')

// POST request
this.http.post<Post>('/api/posts', { title: 'New Post', content: '...' })

// PUT request
this.http.put<Post>(`/api/posts/${id}`, { title: 'Updated' })

// DELETE request
this.http.delete(`/api/posts/${id}`)
```

---

## 📁 Core Directory Structure

```
core/
├── guards/
│   ├── auth.guard.ts       # Protect routes (login required)
│   └── admin.guard.ts      # Protect admin routes
├── interceptors/
│   └── auth.interceptor.ts # Add JWT token to requests
├── models/
│   ├── auth.model.ts       # Login/Register interfaces
│   ├── comment.model.ts    # Comment interface
│   ├── post.model.ts       # Post interface
│   ├── user.model.ts       # User interface
│   └── user-profile.model.ts # UserProfile interface
└── services/
    ├── auth.service.ts     # Authentication API
    ├── comment.service.ts  # Comment API
    ├── like.service.ts     # Like API
    ├── post.service.ts     # Post API
    ├── report.service.ts   # Report API
    ├── subscription.service.ts # Follow API
    └── user.service.ts     # User API
```

---

## 🛠️ Services (core/services/)

### 1. auth.service.ts

**Purpose**: Handle authentication (login, register, logout)

**Key Properties**:
```typescript
private apiUrl = 'http://localhost:8080/api/auth';
private currentUserSubject = new BehaviorSubject<User | null>(null);
public currentUser$ = this.currentUserSubject.asObservable();
```

**What is BehaviorSubject?**
- Like Observable, but holds current value
- Components can get current user anytime
- Updates all subscribers when user changes

**Key Methods**:

#### A. register()
```typescript
register(username: string, email: string, password: string): Observable<AuthResponse> {
  return this.http.post<AuthResponse>(`${this.apiUrl}/register`, {
    username,
    email,
    password
  });
}
```

**What it does**:
1. Sends POST request to `/api/auth/register`
2. Backend creates user
3. Returns JWT tokens
4. Component calls `login()` with tokens

**Usage in component**:
```typescript
this.authService.register(this.username, this.email, this.password)
  .subscribe({
    next: (response) => {
      this.authService.login(response.token, response.refreshToken);
      this.router.navigate(['/home']);
    },
    error: (error) => {
      this.errorMessage = error.error;
    }
  });
```

#### B. login()
```typescript
login(identifier: string, password: string): Observable<AuthResponse> {
  return this.http.post<AuthResponse>(`${this.apiUrl}/login`, {
    identifier,  // username or email
    password
  }).pipe(
    tap(response => {
      // Store tokens
      localStorage.setItem('token', response.token);
      localStorage.setItem('refreshToken', response.refreshToken);
      
      // Load user profile
      this.loadCurrentUser();
    })
  );
}
```

**What is tap()?**
- RxJS operator that runs side effects
- Doesn't modify the stream
- Used here to store tokens and load user

**Flow**:
1. User enters credentials
2. POST to `/api/auth/login`
3. Backend validates
4. Returns tokens
5. `tap()` stores tokens in localStorage
6. `loadCurrentUser()` fetches user profile
7. Component navigates to home

#### C. loadCurrentUser()
```typescript
loadCurrentUser(): void {
  const token = this.getToken();
  if (token) {
    // Decode JWT to get username
    const payload = JSON.parse(atob(token.split('.')[1]));
    const username = payload.sub;
    
    // Fetch full user profile
    this.userService.getUserByUsername(username).subscribe({
      next: (user) => {
        this.currentUserSubject.next(user);
      },
      error: () => {
        this.logout();
      }
    });
  }
}
```

**What it does**:
1. Gets token from localStorage
2. Decodes JWT (base64 decode the middle part)
3. Extracts username from `sub` claim
4. Fetches full user profile from API
5. Updates `currentUserSubject` so all components know who's logged in

**Why decode JWT?**
- JWT contains username
- No need to store username separately
- Token is source of truth

#### D. logout()
```typescript
logout(): void {
  localStorage.removeItem('token');
  localStorage.removeItem('refreshToken');
  this.currentUserSubject.next(null);
  this.router.navigate(['/login']);
}
```

**Simple and clean**:
1. Remove tokens
2. Clear current user
3. Redirect to login

#### E. isAuthenticated()
```typescript
isAuthenticated(): boolean {
  const token = this.getToken();
  return !!token && !this.isTokenExpired(token);
}

private isTokenExpired(token: string): boolean {
  const payload = JSON.parse(atob(token.split('.')[1]));
  const exp = payload.exp * 1000;  // Convert to milliseconds
  return Date.now() > exp;
}
```

**Used by AuthGuard**:
- Check if user logged in before allowing route access

---

### 2. post.service.ts

**Purpose**: CRUD operations for posts

**Key Methods**:

#### A. getAllPosts()
```typescript
getAllPosts(): Observable<Post[]> {
  return this.http.get<Post[]>(`${this.apiUrl}/posts`);
}
```

**Usage**:
```typescript
// In PostListComponent
ngOnInit() {
  this.postService.getAllPosts().subscribe(posts => {
    this.posts = posts;
  });
}
```

#### B. getPostById()
```typescript
getPostById(id: number): Observable<Post> {
  return this.http.get<Post>(`${this.apiUrl}/posts/${id}`);
}
```

**Usage**:
```typescript
// In PostDetailComponent
ngOnInit() {
  const id = this.route.snapshot.params['id'];
  this.postService.getPostById(id).subscribe(post => {
    this.post = post;
  });
}
```

#### C. createPost()
```typescript
createPost(post: CreatePostRequest): Observable<Post> {
  return this.http.post<Post>(`${this.apiUrl}/posts`, post);
}
```

**Usage**:
```typescript
// In CreatePostComponent
onSubmit() {
  const postData = {
    title: this.title,
    content: this.content,
    category: this.category
  };
  
  this.postService.createPost(postData).subscribe({
    next: (post) => {
      this.router.navigate(['/posts', post.id]);
    },
    error: (error) => {
      this.errorMessage = 'Failed to create post';
    }
  });
}
```

#### D. updatePost()
```typescript
updatePost(id: number, post: UpdatePostRequest): Observable<Post> {
  return this.http.put<Post>(`${this.apiUrl}/posts/${id}`, post);
}
```

#### E. deletePost()
```typescript
deletePost(id: number): Observable<void> {
  return this.http.delete<void>(`${this.apiUrl}/posts/${id}`);
}
```

**Usage**:
```typescript
deletePost(postId: number) {
  if (confirm('Are you sure?')) {
    this.postService.deletePost(postId).subscribe({
      next: () => {
        this.posts = this.posts.filter(p => p.id !== postId);
      }
    });
  }
}
```

#### F. getPostsByUser()
```typescript
getPostsByUser(userId: number): Observable<Post[]> {
  return this.http.get<Post[]>(`${this.apiUrl}/posts/user/${userId}`);
}
```

**Usage**: User profile shows their posts

---

### 3. comment.service.ts

**Purpose**: CRUD operations for comments

**Key Methods**:

#### A. getCommentsByPost()
```typescript
getCommentsByPost(postId: number): Observable<Comment[]> {
  return this.http.get<Comment[]>(`${this.apiUrl}/posts/${postId}/comments`);
}
```

**Usage**:
```typescript
// In PostDetailComponent - show comments
loadComments() {
  this.commentService.getCommentsByPost(this.postId)
    .subscribe(comments => {
      this.comments = comments;
    });
}
```

#### B. createComment()
```typescript
createComment(comment: CreateCommentRequest): Observable<Comment> {
  return this.http.post<Comment>(`${this.apiUrl}/comments`, comment);
}
```

**Usage**:
```typescript
addComment() {
  const commentData = {
    postId: this.postId,
    text: this.commentText
  };
  
  this.commentService.createComment(commentData).subscribe({
    next: (comment) => {
      this.comments.push(comment);  // Add to list
      this.commentText = '';        // Clear input
    }
  });
}
```

#### C. updateComment()
```typescript
updateComment(id: number, text: string): Observable<Comment> {
  return this.http.put<Comment>(`${this.apiUrl}/comments/${id}`, { text });
}
```

#### D. deleteComment()
```typescript
deleteComment(id: number): Observable<void> {
  return this.http.delete<void>(`${this.apiUrl}/comments/${id}`);
}
```

---

### 4. like.service.ts

**Purpose**: Like/unlike posts

**Key Methods**:

#### A. likePost()
```typescript
likePost(postId: number): Observable<any> {
  return this.http.post(`${this.apiUrl}/likes/post/${postId}`, {});
}
```

**Usage**:
```typescript
toggleLike() {
  if (this.isLiked) {
    this.likeService.unlikePost(this.postId).subscribe(() => {
      this.isLiked = false;
      this.likesCount--;
    });
  } else {
    this.likeService.likePost(this.postId).subscribe(() => {
      this.isLiked = true;
      this.likesCount++;
    });
  }
}
```

#### B. unlikePost()
```typescript
unlikePost(postId: number): Observable<any> {
  return this.http.delete(`${this.apiUrl}/likes/post/${postId}`);
}
```

#### C. isPostLiked()
```typescript
isPostLiked(postId: number): Observable<boolean> {
  return this.http.get<boolean>(`${this.apiUrl}/likes/post/${postId}/status`);
}
```

**Usage**: Check if current user liked post (for heart icon color)

---

### 5. subscription.service.ts

**Purpose**: Follow/unfollow users

**Key Methods**:

#### A. followUser()
```typescript
followUser(userId: number): Observable<any> {
  return this.http.post(`${this.apiUrl}/subscriptions/follow/${userId}`, {});
}
```

#### B. unfollowUser()
```typescript
unfollowUser(userId: number): Observable<any> {
  return this.http.delete(`${this.apiUrl}/subscriptions/unfollow/${userId}`);
}
```

#### C. isFollowing()
```typescript
isFollowing(userId: number): Observable<boolean> {
  return this.http.get<boolean>(`${this.apiUrl}/subscriptions/following/${userId}`);
}
```

**Usage**:
```typescript
// In ProfileComponent
ngOnInit() {
  this.subscriptionService.isFollowing(this.userId)
    .subscribe(isFollowing => {
      this.isFollowing = isFollowing;
      this.followButtonText = isFollowing ? 'Unfollow' : 'Follow';
    });
}

toggleFollow() {
  if (this.isFollowing) {
    this.subscriptionService.unfollowUser(this.userId).subscribe(() => {
      this.isFollowing = false;
      this.followersCount--;
    });
  } else {
    this.subscriptionService.followUser(this.userId).subscribe(() => {
      this.isFollowing = true;
      this.followersCount++;
    });
  }
}
```

#### D. getFollowers()
```typescript
getFollowers(userId: number): Observable<User[]> {
  return this.http.get<User[]>(`${this.apiUrl}/subscriptions/followers/${userId}`);
}
```

#### E. getFollowing()
```typescript
getFollowing(userId: number): Observable<User[]> {
  return this.http.get<User[]>(`${this.apiUrl}/subscriptions/following/${userId}`);
}
```

---

### 6. user.service.ts

**Purpose**: User profile operations

**Key Methods**:

#### A. getUserByUsername()
```typescript
getUserByUsername(username: string): Observable<User> {
  return this.http.get<User>(`${this.apiUrl}/users/username/${username}`);
}
```

#### B. getUserById()
```typescript
getUserById(id: number): Observable<User> {
  return this.http.get<User>(`${this.apiUrl}/users/${id}`);
}
```

#### C. updateProfile()
```typescript
updateProfile(profile: UpdateProfileRequest): Observable<User> {
  return this.http.put<User>(`${this.apiUrl}/users/profile`, profile);
}
```

**Usage**:
```typescript
// In EditProfileComponent
onSubmit() {
  const profileData = {
    bio: this.bio,
    profilePicture: this.profilePicture,
    coverImage: this.coverImage
  };
  
  this.userService.updateProfile(profileData).subscribe({
    next: (user) => {
      this.router.navigate(['/profile', user.username]);
    }
  });
}
```

---

### 7. report.service.ts

**Purpose**: Report posts, comments, or users

**Key Methods**:

#### A. reportContent()
```typescript
reportContent(report: CreateReportRequest): Observable<any> {
  return this.http.post(`${this.apiUrl}/reports`, report);
}
```

**Usage**:
```typescript
reportPost(postId: number) {
  const reportData = {
    reportType: 'POST',
    contentId: postId,
    reason: 'SPAM',
    description: 'This is spam'
  };
  
  this.reportService.reportContent(reportData).subscribe({
    next: () => {
      alert('Report submitted');
    }
  });
}
```

#### B. getAllReports() (Admin only)
```typescript
getAllReports(): Observable<Report[]> {
  return this.http.get<Report[]>(`${this.apiUrl}/admin/reports`);
}
```

#### C. updateReportStatus() (Admin only)
```typescript
updateReportStatus(reportId: number, status: string, adminNotes: string): Observable<Report> {
  return this.http.put<Report>(`${this.apiUrl}/admin/reports/${reportId}/status`, {
    status,
    adminNotes
  });
}
```

---

## 🎭 Models (core/models/)

### 1. auth.model.ts

```typescript
export interface LoginRequest {
  identifier: string;  // username or email
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  refreshToken: string;
  message?: string;
}
```

**Usage**: Type safety for auth forms

---

### 2. post.model.ts

```typescript
export interface Post {
  id: number;
  title: string;
  content: string;
  category?: string;
  mediaUrl?: string;
  authorId: number;
  authorUsername: string;
  authorProfilePicture?: string;
  likesCount: number;
  commentsCount: number;
  createdAt: Date;
  updatedAt?: Date;
}

export interface CreatePostRequest {
  title: string;
  content: string;
  category?: string;
  mediaUrl?: string;
}

export interface UpdatePostRequest {
  title: string;
  content: string;
  category?: string;
  mediaUrl?: string;
}
```

---

### 3. comment.model.ts

```typescript
export interface Comment {
  id: number;
  text: string;
  userId: number;
  username: string;
  userProfilePicture?: string;
  postId: number;
  createdAt: Date;
  updatedAt?: Date;
}

export interface CreateCommentRequest {
  postId: number;
  text: string;
}

export interface UpdateCommentRequest {
  text: string;
}
```

---

### 4. user.model.ts

```typescript
export interface User {
  id: number;
  username: string;
  email: string;
  role: 'USER' | 'ADMIN';
  bio?: string;
  profilePicture?: string;
  coverImage?: string;
  createdAt: Date;
}

export interface UpdateProfileRequest {
  bio?: string;
  profilePicture?: string;
  coverImage?: string;
}
```

---

### 5. user-profile.model.ts

```typescript
export interface UserProfile {
  id: number;
  username: string;
  email: string;
  bio?: string;
  profilePicture?: string;
  coverImage?: string;
  postsCount: number;
  followersCount: number;
  followingCount: number;
  createdAt: Date;
}
```

**Difference from User**:
- Includes counts (posts, followers, following)
- Used for profile pages

---

## 🛡️ Guards (core/guards/)

### 1. auth.guard.ts

**Purpose**: Protect routes that require authentication

```typescript
@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(): boolean {
    if (this.authService.isAuthenticated()) {
      return true;  // Allow access
    } else {
      this.router.navigate(['/login']);  // Redirect to login
      return false;  // Deny access
    }
  }
}
```

**Usage in routes**:
```typescript
const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { 
    path: 'home', 
    component: HomeComponent,
    canActivate: [AuthGuard]  // ← Protected!
  },
  { 
    path: 'create-post', 
    component: CreatePostComponent,
    canActivate: [AuthGuard]  // ← Protected!
  }
];
```

**Flow**:
1. User tries to access `/home`
2. AuthGuard checks: `isAuthenticated()`?
3. If yes → Allow access
4. If no → Redirect to `/login`

---

### 2. admin.guard.ts

**Purpose**: Protect admin-only routes

```typescript
@Injectable({
  providedIn: 'root'
})
export class AdminGuard implements CanActivate {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(): boolean {
    const currentUser = this.authService.currentUserValue;
    
    if (currentUser && currentUser.role === 'ADMIN') {
      return true;  // Allow access
    } else {
      this.router.navigate(['/']);  // Redirect to home
      return false;  // Deny access
    }
  }
}
```

**Usage**:
```typescript
const routes: Routes = [
  { 
    path: 'admin/reports', 
    component: AdminDashboardComponent,
    canActivate: [AuthGuard, AdminGuard]  // ← Must be logged in AND admin!
  }
];
```

**Flow**:
1. User tries to access `/admin/reports`
2. AuthGuard checks: Authenticated?
3. AdminGuard checks: Role is ADMIN?
4. If both yes → Allow access
5. If either no → Redirect

---

## 🔌 Interceptors (core/interceptors/)

### auth.interceptor.ts

**Purpose**: Automatically add JWT token to every HTTP request

**Problem Without Interceptor**:
```typescript
// Have to manually add token to every request 😫
const headers = new HttpHeaders({
  'Authorization': `Bearer ${this.authService.getToken()}`
});

this.http.get('/api/posts', { headers });
this.http.post('/api/posts', data, { headers });
this.http.put('/api/posts/1', data, { headers });
// ... repeat for every request!
```

**Solution With Interceptor**:
```typescript
// Interceptor adds token automatically! 🎉
this.http.get('/api/posts');  // Token added automatically
this.http.post('/api/posts', data);  // Token added automatically
this.http.put('/api/posts/1', data);  // Token added automatically
```

**Implementation**:
```typescript
@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private authService: AuthService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.authService.getToken();
    
    // Clone request and add Authorization header
    if (token) {
      req = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }
    
    return next.handle(req);
  }
}
```

**What it does**:
1. Intercepts every HTTP request
2. Gets token from AuthService
3. Clones request with Authorization header
4. Passes modified request to next handler

**Why clone?**
- HTTP requests are immutable
- Can't modify original request
- Must clone with changes

**Register in app.config.ts**:
```typescript
export const appConfig: ApplicationConfig = {
  providers: [
    provideHttpClient(
      withInterceptors([authInterceptor])  // ← Register here
    )
  ]
};
```

**Result**:
```
GET /api/posts HTTP/1.1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

Backend sees token and authenticates user automatically!

---

## 🎯 Core Module Flow

### Complete Authentication Flow

```
1. User enters credentials
   ↓
2. LoginComponent calls authService.login()
   ↓
3. AuthService sends POST to /api/auth/login
   ↓
4. AuthInterceptor adds "Authorization: Bearer ..." (if token exists)
   ↓
5. Backend validates credentials
   ↓
6. Backend returns { token, refreshToken }
   ↓
7. AuthService stores tokens in localStorage
   ↓
8. AuthService calls loadCurrentUser()
   ↓
9. UserService fetches user profile
   ↓
10. AuthService updates currentUserSubject
   ↓
11. All components get updated via currentUser$ observable
   ↓
12. Router navigates to /home
   ↓
13. AuthGuard checks: isAuthenticated()? → Yes, allow access
   ↓
14. HomeComponent loads posts via postService.getAllPosts()
   ↓
15. AuthInterceptor adds token to request
   ↓
16. Backend sees token, knows who user is
   ↓
17. Backend returns posts
   ↓
18. HomeComponent displays posts
```

### Protected Route Access Flow

```
1. User clicks "Admin Dashboard" link
   ↓
2. Router tries to navigate to /admin/reports
   ↓
3. AuthGuard.canActivate() runs
   - Checks: isAuthenticated()?
   - If no → Redirect to /login
   ↓
4. AdminGuard.canActivate() runs
   - Checks: role === 'ADMIN'?
   - If no → Redirect to /home
   ↓
5. Both guards pass → Allow access to /admin/reports
   ↓
6. AdminDashboardComponent loads reports
   ↓
7. ReportService sends GET /api/admin/reports
   ↓
8. AuthInterceptor adds token
   ↓
9. Backend checks: Is user ADMIN?
   ↓
10. Backend returns reports
   ↓
11. Component displays reports
```

---

## 📚 Summary

**Core Module = Foundation of Angular App**

- **Services**: API communication logic
  - AuthService: Login, register, logout
  - PostService: CRUD for posts
  - CommentService: CRUD for comments
  - LikeService: Like/unlike
  - SubscriptionService: Follow/unfollow
  - UserService: User profiles
  - ReportService: Report content

- **Models**: TypeScript interfaces
  - Type safety
  - Autocomplete
  - Clear data contracts

- **Guards**: Route protection
  - AuthGuard: Require login
  - AdminGuard: Require admin role

- **Interceptors**: Modify HTTP requests
  - AuthInterceptor: Add JWT token automatically

**All work together** to provide secure, type-safe API communication!
