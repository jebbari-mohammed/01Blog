# Frontend Features Module - Components & Pages

## 📖 What are Features?

**Features** are the main pages and functionality of your app:
- **Auth**: Login and registration pages
- **Home**: Main feed showing all posts
- **Post**: Create, view, edit posts and comments
- **Profile**: User profile page
- **Admin**: Admin dashboard for managing reports

Think of features as the **user-facing parts** of your app.

---

## 🧩 Angular Component Basics

### Component Structure
Every Angular component has 4 files:

```
post-card/
├── post-card.component.ts       # Logic (TypeScript)
├── post-card.component.html     # Template (HTML)
├── post-card.component.scss     # Styles (CSS)
└── post-card.component.spec.ts  # Tests (optional)
```

### Component Anatomy

```typescript
@Component({
  selector: 'app-post-card',           // Use as: <app-post-card></app-post-card>
  templateUrl: './post-card.component.html',
  styleUrls: ['./post-card.component.scss']
})
export class PostCardComponent implements OnInit {
  @Input() post!: Post;                // Input from parent
  @Output() postDeleted = new EventEmitter<number>();  // Output to parent
  
  isLiked = false;
  
  constructor(
    private likeService: LikeService   // Dependency injection
  ) {}
  
  ngOnInit(): void {
    // Runs when component loads
    this.checkIfLiked();
  }
  
  toggleLike(): void {
    // Method called from template
  }
}
```

### Data Binding Types

```html
<!-- 1. Interpolation: Display data -->
<h1>{{ post.title }}</h1>

<!-- 2. Property Binding: Bind to HTML property -->
<img [src]="post.mediaUrl">

<!-- 3. Event Binding: Handle events -->
<button (click)="deletePost()">Delete</button>

<!-- 4. Two-Way Binding: Form inputs -->
<input [(ngModel)]="username">
```

### Lifecycle Hooks

```typescript
ngOnInit()       // Component initialized (load data here)
ngOnDestroy()    // Component destroyed (cleanup here)
ngOnChanges()    // Input properties changed
ngAfterViewInit() // View fully initialized
```

---

## 📁 Features Directory Structure

```
features/
├── auth/
│   ├── login/              # Login page
│   └── register/           # Registration page
├── home/                   # Main feed
├── post/
│   ├── comments/           # Comments section
│   ├── create-post/        # Create new post
│   ├── post-card/          # Post card UI
│   ├── post-create/        # Alternative create post
│   └── post-list/          # List of posts
├── profile/                # User profile page
├── user/
│   ├── edit-profile/       # Edit profile form
│   └── profile/            # User profile view
└── admin/
    ├── admin-dashboard/    # Admin reports dashboard
    └── update-report-status-dialog/  # Update report dialog
```

---

## 🔐 Auth Feature

### 1. Login Component (auth/login/)

**Purpose**: User login form

**Template (login.component.html)**:
```html
<mat-card>
  <mat-card-header>
    <mat-card-title>Login</mat-card-title>
  </mat-card-header>
  
  <mat-card-content>
    <form (ngSubmit)="onSubmit()">
      <!-- Username/Email input -->
      <mat-form-field>
        <mat-label>Username or Email</mat-label>
        <input matInput [(ngModel)]="identifier" name="identifier" required>
      </mat-form-field>
      
      <!-- Password input -->
      <mat-form-field>
        <mat-label>Password</mat-label>
        <input matInput type="password" [(ngModel)]="password" name="password" required>
      </mat-form-field>
      
      <!-- Error message -->
      <div *ngIf="errorMessage" class="error">
        {{ errorMessage }}
      </div>
      
      <!-- Submit button -->
      <button mat-raised-button color="primary" type="submit">
        Login
      </button>
    </form>
  </mat-card-content>
</mat-card>
```

**Component (login.component.ts)**:
```typescript
export class LoginComponent {
  identifier = '';  // username or email
  password = '';
  errorMessage = '';
  
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}
  
  onSubmit(): void {
    this.errorMessage = '';
    
    this.authService.login(this.identifier, this.password)
      .subscribe({
        next: (response) => {
          // Success: navigate to home
          this.router.navigate(['/home']);
        },
        error: (error) => {
          // Error: show message
          this.errorMessage = error.error || 'Login failed';
        }
      });
  }
}
```

**Flow**:
1. User enters identifier (username or email) + password
2. Clicks "Login" button
3. `onSubmit()` called
4. Calls `authService.login()`
5. On success → Navigate to home
6. On error → Show error message

---

### 2. Register Component (auth/register/)

**Purpose**: User registration form

**Template (register.component.html)**:
```html
<mat-card>
  <mat-card-header>
    <mat-card-title>Register</mat-card-title>
  </mat-card-header>
  
  <mat-card-content>
    <form (ngSubmit)="onSubmit()">
      <!-- Username -->
      <mat-form-field>
        <mat-label>Username</mat-label>
        <input matInput [(ngModel)]="username" name="username" required>
        <mat-error *ngIf="errors.username">{{ errors.username }}</mat-error>
      </mat-form-field>
      
      <!-- Email -->
      <mat-form-field>
        <mat-label>Email</mat-label>
        <input matInput type="email" [(ngModel)]="email" name="email" required>
        <mat-error *ngIf="errors.email">{{ errors.email }}</mat-error>
      </mat-form-field>
      
      <!-- Password -->
      <mat-form-field>
        <mat-label>Password</mat-label>
        <input matInput type="password" [(ngModel)]="password" name="password" required>
        <mat-error *ngIf="errors.password">{{ errors.password }}</mat-error>
      </mat-form-field>
      
      <!-- Submit -->
      <button mat-raised-button color="primary" type="submit">
        Register
      </button>
    </form>
  </mat-card-content>
</mat-card>
```

**Component (register.component.ts)**:
```typescript
export class RegisterComponent {
  username = '';
  email = '';
  password = '';
  errors: any = {};
  
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}
  
  onSubmit(): void {
    this.errors = {};
    
    this.authService.register(this.username, this.email, this.password)
      .subscribe({
        next: (response) => {
          // Auto-login after registration
          this.authService.login(response.token, response.refreshToken);
          this.router.navigate(['/home']);
        },
        error: (error) => {
          // Backend returns validation errors
          if (error.error && typeof error.error === 'object') {
            this.errors = error.error;
            // Example: { username: "Username 'john' is already taken" }
          } else {
            this.errors.general = 'Registration failed';
          }
        }
      });
  }
}
```

**Error Handling**:
- Backend returns: `{ username: "Username 'john' is already taken" }`
- Template shows error under username field
- User sees specific problem!

---

## 🏠 Home Feature

### Home Component (home/)

**Purpose**: Main feed showing all posts

**Template (home.component.html)**:
```html
<div class="home-container">
  <!-- Loading spinner -->
  <app-loading-spinner *ngIf="loading"></app-loading-spinner>
  
  <!-- Post list -->
  <div *ngIf="!loading" class="posts-grid">
    <app-post-card 
      *ngFor="let post of posts" 
      [post]="post"
      (postDeleted)="onPostDeleted($event)">
    </app-post-card>
  </div>
  
  <!-- Empty state -->
  <div *ngIf="!loading && posts.length === 0" class="empty-state">
    <p>No posts yet. Create the first one!</p>
    <button mat-raised-button color="primary" routerLink="/create-post">
      Create Post
    </button>
  </div>
</div>
```

**Component (home.component.ts)**:
```typescript
export class HomeComponent implements OnInit {
  posts: Post[] = [];
  loading = true;
  
  constructor(private postService: PostService) {}
  
  ngOnInit(): void {
    this.loadPosts();
  }
  
  loadPosts(): void {
    this.loading = true;
    
    this.postService.getAllPosts().subscribe({
      next: (posts) => {
        this.posts = posts;
        this.loading = false;
      },
      error: (error) => {
        console.error('Failed to load posts', error);
        this.loading = false;
      }
    });
  }
  
  onPostDeleted(postId: number): void {
    // Remove deleted post from list
    this.posts = this.posts.filter(p => p.id !== postId);
  }
}
```

**Flow**:
1. Component loads → `ngOnInit()` called
2. Shows loading spinner
3. Calls `postService.getAllPosts()`
4. Posts returned → Hide spinner, show posts
5. Each post rendered as `<app-post-card>`
6. If post deleted → Remove from list

---

## 📝 Post Feature

### 1. Post Card Component (post/post-card/)

**Purpose**: Display single post with actions (like, comment, delete)

**Template (post-card.component.html)**:
```html
<mat-card class="post-card">
  <!-- Author info -->
  <mat-card-header>
    <img mat-card-avatar [src]="post.authorProfilePicture || 'default-avatar.png'">
    <mat-card-title>{{ post.authorUsername }}</mat-card-title>
    <mat-card-subtitle>{{ post.createdAt | date:'short' }}</mat-card-subtitle>
    
    <!-- Delete button (if owner) -->
    <button 
      *ngIf="isOwner" 
      mat-icon-button 
      (click)="deletePost()"
      class="delete-button">
      <mat-icon>delete</mat-icon>
    </button>
  </mat-card-header>
  
  <!-- Post image -->
  <img 
    *ngIf="post.mediaUrl" 
    mat-card-image 
    [src]="post.mediaUrl">
  
  <!-- Post content -->
  <mat-card-content>
    <h2>{{ post.title }}</h2>
    <p>{{ post.content }}</p>
    <span class="category" *ngIf="post.category">
      {{ post.category }}
    </span>
  </mat-card-content>
  
  <!-- Actions -->
  <mat-card-actions>
    <!-- Like button -->
    <button mat-button (click)="toggleLike()">
      <mat-icon [color]="isLiked ? 'warn' : ''">
        {{ isLiked ? 'favorite' : 'favorite_border' }}
      </mat-icon>
      <span>{{ post.likesCount }}</span>
    </button>
    
    <!-- Comment button -->
    <button mat-button (click)="viewPost()">
      <mat-icon>comment</mat-icon>
      <span>{{ post.commentsCount }}</span>
    </button>
    
    <!-- Report button -->
    <button mat-icon-button (click)="reportPost()">
      <mat-icon>flag</mat-icon>
    </button>
  </mat-card-actions>
</mat-card>
```

**Component (post-card.component.ts)**:
```typescript
export class PostCardComponent implements OnInit {
  @Input() post!: Post;
  @Output() postDeleted = new EventEmitter<number>();
  
  isLiked = false;
  isOwner = false;
  currentUser: User | null = null;
  
  constructor(
    private likeService: LikeService,
    private postService: PostService,
    private authService: AuthService,
    private router: Router,
    private dialog: MatDialog
  ) {}
  
  ngOnInit(): void {
    this.currentUser = this.authService.currentUserValue;
    this.isOwner = this.currentUser?.id === this.post.authorId;
    this.checkIfLiked();
  }
  
  checkIfLiked(): void {
    this.likeService.isPostLiked(this.post.id).subscribe({
      next: (liked) => {
        this.isLiked = liked;
      }
    });
  }
  
  toggleLike(): void {
    if (this.isLiked) {
      // Unlike
      this.likeService.unlikePost(this.post.id).subscribe({
        next: () => {
          this.isLiked = false;
          this.post.likesCount--;
        }
      });
    } else {
      // Like
      this.likeService.likePost(this.post.id).subscribe({
        next: () => {
          this.isLiked = true;
          this.post.likesCount++;
        }
      });
    }
  }
  
  viewPost(): void {
    this.router.navigate(['/posts', this.post.id]);
  }
  
  deletePost(): void {
    if (confirm('Are you sure you want to delete this post?')) {
      this.postService.deletePost(this.post.id).subscribe({
        next: () => {
          this.postDeleted.emit(this.post.id);  // Notify parent
        },
        error: (error) => {
          alert('Failed to delete post');
        }
      });
    }
  }
  
  reportPost(): void {
    const dialogRef = this.dialog.open(ReportDialogComponent, {
      data: {
        reportType: 'POST',
        contentId: this.post.id
      }
    });
  }
}
```

**Key Features**:
- **Like/Unlike**: Toggle heart icon, update count
- **View**: Navigate to post detail page
- **Delete**: Only show if current user is owner
- **Report**: Open dialog to report post

---

### 2. Create Post Component (post/create-post/)

**Purpose**: Form to create new post

**Template (create-post.component.html)**:
```html
<mat-card>
  <mat-card-header>
    <mat-card-title>Create New Post</mat-card-title>
  </mat-card-header>
  
  <mat-card-content>
    <form (ngSubmit)="onSubmit()">
      <!-- Title -->
      <mat-form-field>
        <mat-label>Title</mat-label>
        <input matInput [(ngModel)]="title" name="title" required>
      </mat-form-field>
      
      <!-- Content -->
      <mat-form-field>
        <mat-label>Content</mat-label>
        <textarea 
          matInput 
          [(ngModel)]="content" 
          name="content" 
          rows="10"
          required>
        </textarea>
      </mat-form-field>
      
      <!-- Category -->
      <mat-form-field>
        <mat-label>Category</mat-label>
        <mat-select [(ngModel)]="category" name="category">
          <mat-option value="Technology">Technology</mat-option>
          <mat-option value="Lifestyle">Lifestyle</mat-option>
          <mat-option value="Travel">Travel</mat-option>
          <mat-option value="Food">Food</mat-option>
        </mat-select>
      </mat-form-field>
      
      <!-- Image upload -->
      <div class="image-upload">
        <input 
          type="file" 
          (change)="onImageSelected($event)"
          accept="image/*">
        <img *ngIf="imagePreview" [src]="imagePreview" class="preview">
      </div>
      
      <!-- Submit -->
      <button mat-raised-button color="primary" type="submit">
        Create Post
      </button>
    </form>
  </mat-card-content>
</mat-card>
```

**Component (create-post.component.ts)**:
```typescript
export class CreatePostComponent {
  title = '';
  content = '';
  category = '';
  mediaUrl = '';
  imagePreview: string | null = null;
  
  constructor(
    private postService: PostService,
    private router: Router
  ) {}
  
  onImageSelected(event: any): void {
    const file = event.target.files[0];
    
    if (file) {
      // Create preview
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.imagePreview = e.target.result;
      };
      reader.readAsDataURL(file);
      
      // Upload to server
      this.uploadImage(file);
    }
  }
  
  uploadImage(file: File): void {
    const formData = new FormData();
    formData.append('file', file);
    
    // Call upload service (not shown, but would be similar)
    // this.uploadService.upload(formData).subscribe(...)
    
    // For now, assume we get URL back
    this.mediaUrl = '/uploads/image.jpg';
  }
  
  onSubmit(): void {
    const postData: CreatePostRequest = {
      title: this.title,
      content: this.content,
      category: this.category,
      mediaUrl: this.mediaUrl
    };
    
    this.postService.createPost(postData).subscribe({
      next: (post) => {
        this.router.navigate(['/posts', post.id]);
      },
      error: (error) => {
        alert('Failed to create post');
      }
    });
  }
}
```

**Flow**:
1. User fills form
2. Selects image → Preview shown, uploaded to server
3. Clicks "Create Post"
4. `onSubmit()` called
5. Calls `postService.createPost()`
6. Navigate to new post detail page

---

### 3. Comments Component (post/comments/)

**Purpose**: Display and add comments on post

**Template (comments.component.html)**:
```html
<div class="comments-section">
  <h3>Comments ({{ comments.length }})</h3>
  
  <!-- Add comment form -->
  <div class="add-comment">
    <mat-form-field>
      <mat-label>Add a comment</mat-label>
      <textarea 
        matInput 
        [(ngModel)]="commentText"
        placeholder="Write your comment...">
      </textarea>
    </mat-form-field>
    <button 
      mat-raised-button 
      color="primary" 
      (click)="addComment()"
      [disabled]="!commentText.trim()">
      Post Comment
    </button>
  </div>
  
  <!-- Comments list -->
  <div class="comments-list">
    <div *ngFor="let comment of comments" class="comment">
      <!-- Author info -->
      <div class="comment-header">
        <img [src]="comment.userProfilePicture || 'default-avatar.png'" class="avatar">
        <div>
          <strong>{{ comment.username }}</strong>
          <span class="date">{{ comment.createdAt | date:'short' }}</span>
        </div>
        
        <!-- Delete button (if owner) -->
        <button 
          *ngIf="comment.userId === currentUser?.id"
          mat-icon-button
          (click)="deleteComment(comment.id)">
          <mat-icon>delete</mat-icon>
        </button>
      </div>
      
      <!-- Comment text -->
      <p>{{ comment.text }}</p>
    </div>
  </div>
</div>
```

**Component (comments.component.ts)**:
```typescript
export class CommentsComponent implements OnInit {
  @Input() postId!: number;
  
  comments: Comment[] = [];
  commentText = '';
  currentUser: User | null = null;
  
  constructor(
    private commentService: CommentService,
    private authService: AuthService
  ) {}
  
  ngOnInit(): void {
    this.currentUser = this.authService.currentUserValue;
    this.loadComments();
  }
  
  loadComments(): void {
    this.commentService.getCommentsByPost(this.postId).subscribe({
      next: (comments) => {
        this.comments = comments;
      }
    });
  }
  
  addComment(): void {
    if (!this.commentText.trim()) return;
    
    const commentData: CreateCommentRequest = {
      postId: this.postId,
      text: this.commentText
    };
    
    this.commentService.createComment(commentData).subscribe({
      next: (comment) => {
        this.comments.push(comment);  // Add to list
        this.commentText = '';         // Clear input
      },
      error: (error) => {
        alert('Failed to add comment');
      }
    });
  }
  
  deleteComment(commentId: number): void {
    if (confirm('Delete this comment?')) {
      this.commentService.deleteComment(commentId).subscribe({
        next: () => {
          this.comments = this.comments.filter(c => c.id !== commentId);
        }
      });
    }
  }
}
```

**Usage**:
```html
<!-- In PostDetailComponent -->
<app-post-card [post]="post"></app-post-card>
<app-comments [postId]="post.id"></app-comments>
```

---

## 👤 Profile Feature

### Profile Component (profile/)

**Purpose**: Display user profile with posts, followers, following

**Template (profile.component.html)**:
```html
<div class="profile-container">
  <!-- Cover image -->
  <div class="cover-image" [style.background-image]="'url(' + user.coverImage + ')'">
  </div>
  
  <!-- Profile header -->
  <div class="profile-header">
    <img [src]="user.profilePicture || 'default-avatar.png'" class="profile-picture">
    
    <div class="profile-info">
      <h1>{{ user.username }}</h1>
      <p>{{ user.bio }}</p>
      
      <!-- Stats -->
      <div class="stats">
        <div class="stat">
          <span class="count">{{ user.postsCount }}</span>
          <span>Posts</span>
        </div>
        <div class="stat" (click)="showFollowers()">
          <span class="count">{{ user.followersCount }}</span>
          <span>Followers</span>
        </div>
        <div class="stat" (click)="showFollowing()">
          <span class="count">{{ user.followingCount }}</span>
          <span>Following</span>
        </div>
      </div>
      
      <!-- Actions -->
      <div class="actions">
        <!-- If viewing own profile -->
        <button 
          *ngIf="isOwnProfile" 
          mat-raised-button 
          routerLink="/edit-profile">
          Edit Profile
        </button>
        
        <!-- If viewing other user's profile -->
        <div *ngIf="!isOwnProfile">
          <button 
            mat-raised-button 
            [color]="isFollowing ? 'accent' : 'primary'"
            (click)="toggleFollow()">
            {{ isFollowing ? 'Unfollow' : 'Follow' }}
          </button>
          
          <button mat-icon-button (click)="reportUser()">
            <mat-icon>flag</mat-icon>
          </button>
        </div>
      </div>
    </div>
  </div>
  
  <!-- User's posts -->
  <div class="user-posts">
    <h2>Posts</h2>
    <div class="posts-grid">
      <app-post-card 
        *ngFor="let post of posts" 
        [post]="post">
      </app-post-card>
    </div>
  </div>
</div>
```

**Component (profile.component.ts)**:
```typescript
export class ProfileComponent implements OnInit {
  user!: UserProfile;
  posts: Post[] = [];
  isOwnProfile = false;
  isFollowing = false;
  currentUser: User | null = null;
  
  constructor(
    private route: ActivatedRoute,
    private userService: UserService,
    private postService: PostService,
    private subscriptionService: SubscriptionService,
    private authService: AuthService,
    private dialog: MatDialog
  ) {}
  
  ngOnInit(): void {
    this.currentUser = this.authService.currentUserValue;
    
    // Get username from URL
    this.route.params.subscribe(params => {
      const username = params['username'];
      this.loadProfile(username);
    });
  }
  
  loadProfile(username: string): void {
    this.userService.getUserByUsername(username).subscribe({
      next: (user) => {
        this.user = user;
        this.isOwnProfile = this.currentUser?.id === user.id;
        
        if (!this.isOwnProfile) {
          this.checkIfFollowing();
        }
        
        this.loadUserPosts();
      }
    });
  }
  
  loadUserPosts(): void {
    this.postService.getPostsByUser(this.user.id).subscribe({
      next: (posts) => {
        this.posts = posts;
      }
    });
  }
  
  checkIfFollowing(): void {
    this.subscriptionService.isFollowing(this.user.id).subscribe({
      next: (following) => {
        this.isFollowing = following;
      }
    });
  }
  
  toggleFollow(): void {
    if (this.isFollowing) {
      this.subscriptionService.unfollowUser(this.user.id).subscribe({
        next: () => {
          this.isFollowing = false;
          this.user.followersCount--;
        }
      });
    } else {
      this.subscriptionService.followUser(this.user.id).subscribe({
        next: () => {
          this.isFollowing = true;
          this.user.followersCount++;
        }
      });
    }
  }
  
  showFollowers(): void {
    this.dialog.open(UserListDialogComponent, {
      data: {
        userId: this.user.id,
        type: 'followers',
        title: 'Followers'
      }
    });
  }
  
  showFollowing(): void {
    this.dialog.open(UserListDialogComponent, {
      data: {
        userId: this.user.id,
        type: 'following',
        title: 'Following'
      }
    });
  }
  
  reportUser(): void {
    this.dialog.open(ReportDialogComponent, {
      data: {
        reportType: 'USER',
        contentId: this.user.id
      }
    });
  }
}
```

**Features**:
- View user info (profile picture, bio, stats)
- Follow/unfollow button (if not own profile)
- Edit profile button (if own profile)
- View user's posts
- Click stats to see followers/following in dialog
- Report user button

---

### Edit Profile Component (user/edit-profile/)

**Purpose**: Form to update user profile

**Template (edit-profile.component.html)**:
```html
<mat-card>
  <mat-card-header>
    <mat-card-title>Edit Profile</mat-card-title>
  </mat-card-header>
  
  <mat-card-content>
    <form (ngSubmit)="onSubmit()">
      <!-- Profile picture -->
      <div class="image-upload">
        <label>Profile Picture</label>
        <img [src]="profilePicture || 'default-avatar.png'" class="preview">
        <input type="file" (change)="onProfilePictureSelected($event)">
      </div>
      
      <!-- Cover image -->
      <div class="image-upload">
        <label>Cover Image</label>
        <img [src]="coverImage" class="preview">
        <input type="file" (change)="onCoverImageSelected($event)">
      </div>
      
      <!-- Bio -->
      <mat-form-field>
        <mat-label>Bio</mat-label>
        <textarea 
          matInput 
          [(ngModel)]="bio" 
          name="bio"
          rows="4"
          maxlength="500">
        </textarea>
        <mat-hint>{{ bio.length }}/500</mat-hint>
      </mat-form-field>
      
      <!-- Submit -->
      <button mat-raised-button color="primary" type="submit">
        Save Changes
      </button>
      <button mat-button type="button" routerLink="/profile">
        Cancel
      </button>
    </form>
  </mat-card-content>
</mat-card>
```

**Component (edit-profile.component.ts)**:
```typescript
export class EditProfileComponent implements OnInit {
  bio = '';
  profilePicture = '';
  coverImage = '';
  
  constructor(
    private userService: UserService,
    private authService: AuthService,
    private router: Router
  ) {}
  
  ngOnInit(): void {
    const currentUser = this.authService.currentUserValue;
    if (currentUser) {
      this.bio = currentUser.bio || '';
      this.profilePicture = currentUser.profilePicture || '';
      this.coverImage = currentUser.coverImage || '';
    }
  }
  
  onProfilePictureSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      // Upload and set URL
      this.uploadImage(file, 'profile');
    }
  }
  
  onCoverImageSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.uploadImage(file, 'cover');
    }
  }
  
  uploadImage(file: File, type: 'profile' | 'cover'): void {
    // Upload logic here
    // For now, assume we get URL back
    const url = '/uploads/' + file.name;
    
    if (type === 'profile') {
      this.profilePicture = url;
    } else {
      this.coverImage = url;
    }
  }
  
  onSubmit(): void {
    const profileData: UpdateProfileRequest = {
      bio: this.bio,
      profilePicture: this.profilePicture,
      coverImage: this.coverImage
    };
    
    this.userService.updateProfile(profileData).subscribe({
      next: (user) => {
        // Update current user in auth service
        this.authService.updateCurrentUser(user);
        
        // Navigate to profile
        this.router.navigate(['/profile', user.username]);
      },
      error: (error) => {
        alert('Failed to update profile');
      }
    });
  }
}
```

---

## 👑 Admin Feature

### Admin Dashboard Component (admin/admin-dashboard/)

**Purpose**: View and manage reports

**Template (admin-dashboard.component.html)**:
```html
<div class="admin-dashboard">
  <h1>Admin Dashboard - Reports</h1>
  
  <!-- Filter -->
  <mat-form-field>
    <mat-label>Filter by status</mat-label>
    <mat-select [(ngModel)]="filterStatus" (selectionChange)="filterReports()">
      <mat-option value="ALL">All</mat-option>
      <mat-option value="PENDING">Pending</mat-option>
      <mat-option value="REVIEWED">Reviewed</mat-option>
      <mat-option value="RESOLVED">Resolved</mat-option>
      <mat-option value="DISMISSED">Dismissed</mat-option>
    </mat-select>
  </mat-form-field>
  
  <!-- Reports table -->
  <table mat-table [dataSource]="filteredReports" class="mat-elevation-z8">
    <!-- ID Column -->
    <ng-container matColumnDef="id">
      <th mat-header-cell *matHeaderCellDef>ID</th>
      <td mat-cell *matCellDef="let report">{{ report.id }}</td>
    </ng-container>
    
    <!-- Type Column -->
    <ng-container matColumnDef="type">
      <th mat-header-cell *matHeaderCellDef>Type</th>
      <td mat-cell *matCellDef="let report">
        <mat-chip>{{ report.reportType }}</mat-chip>
      </td>
    </ng-container>
    
    <!-- Reason Column -->
    <ng-container matColumnDef="reason">
      <th mat-header-cell *matHeaderCellDef>Reason</th>
      <td mat-cell *matCellDef="let report">{{ report.reason }}</td>
    </ng-container>
    
    <!-- Reporter Column -->
    <ng-container matColumnDef="reporter">
      <th mat-header-cell *matHeaderCellDef>Reporter</th>
      <td mat-cell *matCellDef="let report">{{ report.reporterUsername }}</td>
    </ng-container>
    
    <!-- Content Author Column -->
    <ng-container matColumnDef="author">
      <th mat-header-cell *matHeaderCellDef>Content Author</th>
      <td mat-cell *matCellDef="let report">{{ report.contentAuthor }}</td>
    </ng-container>
    
    <!-- Status Column -->
    <ng-container matColumnDef="status">
      <th mat-header-cell *matHeaderCellDef>Status</th>
      <td mat-cell *matCellDef="let report">
        <mat-chip [color]="getStatusColor(report.status)">
          {{ report.status }}
        </mat-chip>
      </td>
    </ng-container>
    
    <!-- Date Column -->
    <ng-container matColumnDef="date">
      <th mat-header-cell *matHeaderCellDef>Date</th>
      <td mat-cell *matCellDef="let report">
        {{ report.createdAt | date:'short' }}
      </td>
    </ng-container>
    
    <!-- Actions Column -->
    <ng-container matColumnDef="actions">
      <th mat-header-cell *matHeaderCellDef>Actions</th>
      <td mat-cell *matCellDef="let report">
        <button mat-button (click)="viewReport(report)">View</button>
        <button mat-button (click)="updateStatus(report)">Update</button>
      </td>
    </ng-container>
    
    <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
    <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
  </table>
</div>
```

**Component (admin-dashboard.component.ts)**:
```typescript
export class AdminDashboardComponent implements OnInit {
  reports: Report[] = [];
  filteredReports: Report[] = [];
  filterStatus = 'ALL';
  displayedColumns = ['id', 'type', 'reason', 'reporter', 'author', 'status', 'date', 'actions'];
  
  constructor(
    private reportService: ReportService,
    private dialog: MatDialog
  ) {}
  
  ngOnInit(): void {
    this.loadReports();
  }
  
  loadReports(): void {
    this.reportService.getAllReports().subscribe({
      next: (reports) => {
        this.reports = reports;
        this.filterReports();
      }
    });
  }
  
  filterReports(): void {
    if (this.filterStatus === 'ALL') {
      this.filteredReports = this.reports;
    } else {
      this.filteredReports = this.reports.filter(
        r => r.status === this.filterStatus
      );
    }
  }
  
  getStatusColor(status: string): string {
    switch (status) {
      case 'PENDING': return 'warn';
      case 'REVIEWED': return 'accent';
      case 'RESOLVED': return 'primary';
      case 'DISMISSED': return '';
      default: return '';
    }
  }
  
  viewReport(report: Report): void {
    // Show report details in dialog
    alert(`Report #${report.id}\nType: ${report.reportType}\nReason: ${report.reason}\nDescription: ${report.description}`);
  }
  
  updateStatus(report: Report): void {
    const dialogRef = this.dialog.open(UpdateReportStatusDialogComponent, {
      data: { report }
    });
    
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadReports();  // Reload after update
      }
    });
  }
}
```

**Features**:
- Table showing all reports
- Filter by status (PENDING, REVIEWED, etc.)
- Color-coded status chips
- View report details
- Update report status (opens dialog)

---

## 📚 Summary

**Features = User-Facing Pages**

### Auth
- **Login**: User authentication
- **Register**: User registration with validation

### Home
- **Feed**: Display all posts
- **Loading states**: Spinner while loading
- **Empty states**: Message when no posts

### Post
- **Post Card**: Display single post with actions
- **Create Post**: Form to create new post
- **Comments**: View and add comments
- **Like/Unlike**: Toggle heart icon
- **Delete**: Remove own posts

### Profile
- **View Profile**: User info, stats, posts
- **Edit Profile**: Update bio, pictures
- **Follow/Unfollow**: Connect with users
- **Followers/Following**: View lists in dialog

### Admin
- **Dashboard**: View all reports
- **Filter**: By status
- **Update Status**: Resolve/dismiss reports

**Components communicate via**:
- `@Input()` - Parent → Child
- `@Output()` - Child → Parent
- Services - Shared state
- Router - Navigation

All features use **Angular Material** for consistent UI!
