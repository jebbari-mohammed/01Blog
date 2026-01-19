# Frontend Shared Module - Reusable Components & Utilities

> **Status:** ✅ Complete | **Last Updated:** January 17, 2026

## 📖 What is the Shared Module?

The **Shared module** contains reusable components and utilities:
- **Components**: UI components used across features
- **Material Module**: Angular Material configuration
- **Dialogs**: Popup windows for user interactions
- **Navbar**: Navigation with notification bell ✅
- **Notification Panel**: Instagram-inspired dropdown ✅

Think of Shared as the **toolbox** of reusable pieces.

---

## ✅ Implemented Shared Components

### 1. Navbar Component (`components/navbar/`)
**Features:**
- ✅ Logo and app name
- ✅ Navigation links (Home, Profile, Create Post, Admin)
- ✅ **Notification bell icon with unread badge** ✅
- ✅ User menu dropdown
- ✅ Logout functionality
- ✅ Admin badge for admin users
- ✅ Responsive design
- ✅ No debug logs or debug text

**Key Implementation:**
```typescript
export class NavbarComponent {
  currentUser: User | null = null;
  unreadCount$ = this.notificationService.unreadCount$;
  
  // Notification bell with badge
  // Opens NotificationPanelComponent in mat-menu
}
```

### 2. Notification Panel Component (`components/notification-panel/`) ✅
**Features:**
- ✅ **Instagram-inspired design**
- ✅ 480px wide dropdown panel
- ✅ **Gradient icon backgrounds:**
  - Rainbow gradient for FOLLOW
  - Purple gradient for LIKE/COMMENT
  - Orange gradient for NEW_POST
- ✅ Auto-polling every 30 seconds
- ✅ Unread notification highlighting
- ✅ Mark as read (individual or all)
- ✅ Navigate to specific post or profile
- ✅ Loading state with spinner
- ✅ Empty state with icon
- ✅ **Text wrapping (no horizontal scroll)** ✅
- ✅ Smooth animations and hover effects
- ✅ Custom scrollbar styling

**Key Implementation:**
```typescript
export class NotificationPanelComponent {
  notifications$ = this.notificationService.notifications$;
  isLoading = false;
  
  getNotificationIcon(type): string {
    // Returns icon based on type
  }
  
  getNotificationLink(notification): string {
    // Returns /posts/:id or /profile/:username
  }
  
  markAsRead(id): void {
    // Mark individual notification as read
  }
  
  markAllAsRead(): void {
    // Mark all notifications as read
  }
}
```

### 3. Material Module (`material.module.ts`)
**Purpose:** Centralize Angular Material imports

**Included Components:**
- Buttons, Cards, Forms, Inputs
- Icons, Toolbar, Menu, Sidenav
- Dialogs, Snackbar, Progress
- Tables, Paginator, Sort
- **Badge** (for notification count)
- **Progress Spinner** (for loading)

---

## 🧩 Why Shared Module?

### Problem Without Shared Module:
```
LoginComponent imports MatButtonModule
RegisterComponent imports MatButtonModule
HomeComponent imports MatButtonModule
ProfileComponent imports MatButtonModule
... (repeat 20+ times)
```

### Solution With Shared Module:
```typescript
// material.module.ts - Import once
@NgModule({
  imports: [MatButtonModule, MatCardModule, ...],
  exports: [MatButtonModule, MatCardModule, ...]
})
export class MaterialModule {}

// Every component just imports MaterialModule
@Component({...})
export class AnyComponent {
  // All Material components available!
}
```

**Benefits**:
- **DRY**: Don't Repeat Yourself
- **Maintenance**: Update imports in one place
- **Consistency**: Same UI components everywhere

---

## 📁 Shared Directory Structure

```
shared/
├── material.module.ts          # Angular Material imports
└── components/
    ├── navbar/                 # Navigation bar
    ├── loading-spinner/        # Loading indicator
    ├── report-dialog/          # Report content dialog
    └── user-list-dialog/       # Followers/following list dialog
```

---

## 🎨 Material Module (material.module.ts)

**Purpose**: Centralize Angular Material component imports

**Full Implementation**:
```typescript
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatMenuModule } from '@angular/material/menu';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSortModule } from '@angular/material/sort';
import { MatDialogModule } from '@angular/material/dialog';
import { MatSelectModule } from '@angular/material/select';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatBadgeModule } from '@angular/material/badge';
import { MatTooltipModule } from '@angular/material/tooltip';

const MaterialComponents = [
  MatButtonModule,
  MatCardModule,
  MatInputModule,
  MatFormFieldModule,
  MatIconModule,
  MatToolbarModule,
  MatMenuModule,
  MatSidenavModule,
  MatListModule,
  MatTableModule,
  MatPaginatorModule,
  MatSortModule,
  MatDialogModule,
  MatSelectModule,
  MatChipsModule,
  MatProgressSpinnerModule,
  MatSnackBarModule,
  MatBadgeModule,
  MatTooltipModule
];

@NgModule({
  imports: MaterialComponents,    // Import into this module
  exports: MaterialComponents     // Export for other modules to use
})
export class MaterialModule {}
```

**What Each Module Provides**:

### Buttons & Interaction
- **MatButtonModule**: Buttons (`<button mat-button>`, `<button mat-raised-button>`)
- **MatIconModule**: Icons (`<mat-icon>favorite</mat-icon>`)
- **MatMenuModule**: Dropdown menus

### Forms
- **MatFormFieldModule**: Form field wrapper
- **MatInputModule**: Input fields (`<input matInput>`)
- **MatSelectModule**: Dropdown select (`<mat-select>`)

### Layout
- **MatCardModule**: Cards (`<mat-card>`)
- **MatToolbarModule**: Top navigation bar
- **MatSidenavModule**: Side drawer
- **MatListModule**: Lists (`<mat-list>`)

### Data Display
- **MatTableModule**: Data tables
- **MatPaginatorModule**: Pagination for tables
- **MatSortModule**: Sorting for tables
- **MatChipsModule**: Chips/tags (`<mat-chip>`)
- **MatBadgeModule**: Notification badges

### Feedback
- **MatProgressSpinnerModule**: Loading spinners
- **MatSnackBarModule**: Toast notifications
- **MatTooltipModule**: Hover tooltips

### Overlays
- **MatDialogModule**: Modal dialogs

**Usage**:
```typescript
// In any component
import { MaterialModule } from './shared/material.module';

@Component({
  selector: 'app-my-component',
  standalone: true,
  imports: [MaterialModule],  // ← Import once, use all Material components
  templateUrl: './my-component.component.html'
})
export class MyComponent {}
```

---

## 🧩 Shared Components

### 1. Navbar Component (shared/components/navbar/)

**Purpose**: Top navigation bar present on all pages

**Template (navbar.component.html)**:
```html
<mat-toolbar color="primary" class="navbar">
  <!-- Logo -->
  <button mat-button routerLink="/home" class="logo">
    <mat-icon>home</mat-icon>
    <span>Blog App</span>
  </button>
  
  <!-- Spacer pushes everything after it to the right -->
  <span class="spacer"></span>
  
  <!-- Navigation links (if logged in) -->
  <ng-container *ngIf="isLoggedIn">
    <!-- Home -->
    <button mat-button routerLink="/home" routerLinkActive="active">
      <mat-icon>home</mat-icon>
      <span>Home</span>
    </button>
    
    <!-- Create Post -->
    <button mat-button routerLink="/create-post" routerLinkActive="active">
      <mat-icon>add_circle</mat-icon>
      <span>Create</span>
    </button>
    
    <!-- Profile Menu -->
    <button mat-button [matMenuTriggerFor]="profileMenu">
      <img 
        [src]="currentUser?.profilePicture || 'default-avatar.png'" 
        class="avatar">
      <span>{{ currentUser?.username }}</span>
      <mat-icon>arrow_drop_down</mat-icon>
    </button>
    <mat-menu #profileMenu="matMenu">
      <button mat-menu-item routerLink="/profile/{{ currentUser?.username }}">
        <mat-icon>person</mat-icon>
        <span>My Profile</span>
      </button>
      <button mat-menu-item routerLink="/edit-profile">
        <mat-icon>edit</mat-icon>
        <span>Edit Profile</span>
      </button>
      
      <!-- Admin link (if admin) -->
      <button 
        *ngIf="currentUser?.role === 'ADMIN'" 
        mat-menu-item 
        routerLink="/admin/reports">
        <mat-icon>admin_panel_settings</mat-icon>
        <span>Admin Dashboard</span>
      </button>
      
      <mat-divider></mat-divider>
      
      <button mat-menu-item (click)="logout()">
        <mat-icon>logout</mat-icon>
        <span>Logout</span>
      </button>
    </mat-menu>
  </ng-container>
  
  <!-- Login/Register links (if not logged in) -->
  <ng-container *ngIf="!isLoggedIn">
    <button mat-button routerLink="/login">Login</button>
    <button mat-raised-button color="accent" routerLink="/register">
      Register
    </button>
  </ng-container>
</mat-toolbar>
```

**Component (navbar.component.ts)**:
```typescript
export class NavbarComponent implements OnInit, OnDestroy {
  isLoggedIn = false;
  currentUser: User | null = null;
  private subscription: Subscription | null = null;
  
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}
  
  ngOnInit(): void {
    // Subscribe to current user changes
    this.subscription = this.authService.currentUser$.subscribe({
      next: (user) => {
        this.currentUser = user;
        this.isLoggedIn = !!user;  // !! converts to boolean
      }
    });
  }
  
  ngOnDestroy(): void {
    // Cleanup subscription to prevent memory leaks
    this.subscription?.unsubscribe();
  }
  
  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
```

**SCSS (navbar.component.scss)**:
```scss
.navbar {
  position: sticky;
  top: 0;
  z-index: 1000;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.logo {
  font-size: 1.2rem;
  font-weight: bold;
  
  mat-icon {
    margin-right: 8px;
  }
}

.spacer {
  flex: 1 1 auto;  // Takes up all available space
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  margin-right: 8px;
}

.active {
  background-color: rgba(255, 255, 255, 0.1);
}
```

**Key Features**:
- **Responsive**: Shows different content based on login state
- **Dynamic**: Updates when user logs in/out (via subscription)
- **Admin Access**: Shows admin link only for admin users
- **Active Route**: Highlights current page with `routerLinkActive`

**Why Subscribe to currentUser$?**
```typescript
// When user logs in
authService.login() 
  → Updates currentUserSubject
  → Navbar receives update via subscription
  → Navbar shows user menu automatically!
```

**Memory Leak Prevention**:
```typescript
ngOnDestroy() {
  this.subscription?.unsubscribe();  // ← Important!
}
```
Without unsubscribe, subscription continues even after component destroyed!

---

### 2. Loading Spinner Component (shared/components/loading-spinner/)

**Purpose**: Show loading indicator during API calls

**Template (loading-spinner.component.html)**:
```html
<div class="spinner-container">
  <mat-spinner></mat-spinner>
  <p *ngIf="message">{{ message }}</p>
</div>
```

**Component (loading-spinner.component.ts)**:
```typescript
export class LoadingSpinnerComponent {
  @Input() message = 'Loading...';
  @Input() diameter = 50;  // Spinner size
}
```

**SCSS (loading-spinner.component.scss)**:
```scss
.spinner-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  
  p {
    margin-top: 16px;
    color: rgba(0, 0, 0, 0.6);
  }
}
```

**Usage**:
```html
<!-- In HomeComponent -->
<app-loading-spinner 
  *ngIf="loading" 
  message="Loading posts...">
</app-loading-spinner>

<div *ngIf="!loading">
  <!-- Content here -->
</div>
```

**Conditional Rendering**:
```typescript
// Component
loading = true;

ngOnInit() {
  this.postService.getAllPosts().subscribe({
    next: (posts) => {
      this.posts = posts;
      this.loading = false;  // Hide spinner
    }
  });
}
```

---

### 3. Report Dialog Component (shared/components/report-dialog/)

**Purpose**: Dialog to report posts, comments, or users

**Template (report-dialog.component.html)**:
```html
<h2 mat-dialog-title>Report {{ data.reportType }}</h2>

<mat-dialog-content>
  <form [formGroup]="reportForm">
    <!-- Reason selection -->
    <mat-form-field>
      <mat-label>Reason</mat-label>
      <mat-select formControlName="reason" required>
        <mat-option value="SPAM">Spam</mat-option>
        <mat-option value="HARASSMENT">Harassment</mat-option>
        <mat-option value="HATE_SPEECH">Hate Speech</mat-option>
        <mat-option value="VIOLENCE">Violence</mat-option>
        <mat-option value="MISINFORMATION">Misinformation</mat-option>
        <mat-option value="OTHER">Other</mat-option>
      </mat-select>
    </mat-form-field>
    
    <!-- Description -->
    <mat-form-field>
      <mat-label>Additional Details (Optional)</mat-label>
      <textarea 
        matInput 
        formControlName="description"
        rows="4"
        placeholder="Provide more information about this report...">
      </textarea>
    </mat-form-field>
  </form>
</mat-dialog-content>

<mat-dialog-actions align="end">
  <button mat-button mat-dialog-close>Cancel</button>
  <button 
    mat-raised-button 
    color="warn" 
    (click)="submitReport()"
    [disabled]="!reportForm.valid">
    Submit Report
  </button>
</mat-dialog-actions>
```

**Component (report-dialog.component.ts)**:
```typescript
export class ReportDialogComponent implements OnInit {
  reportForm!: FormGroup;
  
  constructor(
    public dialogRef: MatDialogRef<ReportDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { 
      reportType: 'POST' | 'COMMENT' | 'USER',
      contentId: number 
    },
    private fb: FormBuilder,
    private reportService: ReportService,
    private snackBar: MatSnackBar
  ) {}
  
  ngOnInit(): void {
    this.reportForm = this.fb.group({
      reason: ['', Validators.required],
      description: ['']
    });
  }
  
  submitReport(): void {
    if (this.reportForm.invalid) return;
    
    const reportData: CreateReportRequest = {
      reportType: this.data.reportType,
      contentId: this.data.contentId,
      reason: this.reportForm.value.reason,
      description: this.reportForm.value.description
    };
    
    this.reportService.reportContent(reportData).subscribe({
      next: () => {
        this.snackBar.open('Report submitted successfully', 'Close', {
          duration: 3000
        });
        this.dialogRef.close(true);
      },
      error: (error) => {
        this.snackBar.open(
          error.error || 'Failed to submit report', 
          'Close', 
          { duration: 3000 }
        );
      }
    });
  }
}
```

**What is MAT_DIALOG_DATA?**
- Special token to inject data passed to dialog
- When opening dialog:
```typescript
this.dialog.open(ReportDialogComponent, {
  data: {
    reportType: 'POST',
    contentId: 5
  }
});
```
- Inside dialog, access via `@Inject(MAT_DIALOG_DATA)`

**What is FormBuilder?**
- Service to create reactive forms easily
- Instead of:
```typescript
new FormGroup({
  reason: new FormControl('', Validators.required),
  description: new FormControl('')
})
```
- Use:
```typescript
this.fb.group({
  reason: ['', Validators.required],
  description: ['']
})
```

**What is MatSnackBar?**
- Toast notification at bottom of screen
- Shows feedback: "Report submitted successfully"
- Auto-closes after duration

**Dialog Flow**:
```
1. User clicks "Report" button on post
2. Component opens dialog:
   this.dialog.open(ReportDialogComponent, { data: {...} })
3. Dialog appears with form
4. User selects reason, enters description
5. Clicks "Submit Report"
6. API call to backend
7. Success → Show toast, close dialog
8. Error → Show error toast, keep dialog open
```

---

### 4. User List Dialog Component (shared/components/user-list-dialog/)

**Purpose**: Show list of followers or following users

**Template (user-list-dialog.component.html)**:
```html
<h2 mat-dialog-title>{{ data.title }}</h2>

<mat-dialog-content>
  <!-- Loading spinner -->
  <app-loading-spinner *ngIf="loading"></app-loading-spinner>
  
  <!-- User list -->
  <mat-list *ngIf="!loading">
    <mat-list-item *ngFor="let user of users">
      <!-- Avatar -->
      <img 
        matListItemAvatar 
        [src]="user.profilePicture || 'default-avatar.png'">
      
      <!-- User info -->
      <div matListItemTitle>
        <a [routerLink]="['/profile', user.username]">
          {{ user.username }}
        </a>
      </div>
      <div matListItemLine>{{ user.bio }}</div>
      
      <!-- Actions -->
      <div matListItemMeta>
        <!-- Follow/Unfollow button -->
        <button 
          mat-button 
          *ngIf="user.id !== currentUser?.id"
          [color]="user.isFollowing ? 'accent' : 'primary'"
          (click)="toggleFollow(user)">
          {{ user.isFollowing ? 'Unfollow' : 'Follow' }}
        </button>
        
        <!-- Report button -->
        <button 
          mat-icon-button 
          *ngIf="user.id !== currentUser?.id"
          (click)="reportUser(user)">
          <mat-icon>flag</mat-icon>
        </button>
      </div>
    </mat-list-item>
  </mat-list>
  
  <!-- Empty state -->
  <div *ngIf="!loading && users.length === 0" class="empty-state">
    <p>No users to show</p>
  </div>
</mat-dialog-content>

<mat-dialog-actions align="end">
  <button mat-button mat-dialog-close>Close</button>
</mat-dialog-actions>
```

**Component (user-list-dialog.component.ts)**:
```typescript
export class UserListDialogComponent implements OnInit {
  users: UserSummary[] = [];
  loading = true;
  currentUser: User | null = null;
  
  constructor(
    public dialogRef: MatDialogRef<UserListDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: {
      userId: number,
      type: 'followers' | 'following',
      title: string
    },
    private subscriptionService: SubscriptionService,
    private authService: AuthService,
    private dialog: MatDialog
  ) {}
  
  ngOnInit(): void {
    this.currentUser = this.authService.currentUserValue;
    this.loadUsers();
  }
  
  loadUsers(): void {
    this.loading = true;
    
    const observable = this.data.type === 'followers'
      ? this.subscriptionService.getFollowers(this.data.userId)
      : this.subscriptionService.getFollowing(this.data.userId);
    
    observable.subscribe({
      next: (users) => {
        this.users = users;
        this.loading = false;
      },
      error: (error) => {
        console.error('Failed to load users', error);
        this.loading = false;
      }
    });
  }
  
  toggleFollow(user: UserSummary): void {
    if (user.isFollowing) {
      // Unfollow
      this.subscriptionService.unfollowUser(user.id).subscribe({
        next: () => {
          user.isFollowing = false;
        }
      });
    } else {
      // Follow
      this.subscriptionService.followUser(user.id).subscribe({
        next: () => {
          user.isFollowing = true;
        }
      });
    }
  }
  
  reportUser(user: UserSummary): void {
    this.dialog.open(ReportDialogComponent, {
      data: {
        reportType: 'USER',
        contentId: user.id
      }
    });
  }
}
```

**SCSS (user-list-dialog.component.scss)**:
```scss
mat-dialog-content {
  min-width: 400px;
  max-height: 500px;
  overflow-y: auto;
}

mat-list-item {
  border-bottom: 1px solid #e0e0e0;
  
  &:last-child {
    border-bottom: none;
  }
  
  a {
    color: #1976d2;
    text-decoration: none;
    font-weight: 500;
    
    &:hover {
      text-decoration: underline;
    }
  }
}

.empty-state {
  padding: 40px;
  text-align: center;
  color: rgba(0, 0, 0, 0.6);
}
```

**Usage**:
```typescript
// In ProfileComponent - show followers
showFollowers() {
  this.dialog.open(UserListDialogComponent, {
    data: {
      userId: this.user.id,
      type: 'followers',
      title: 'Followers'
    }
  });
}

// Show following
showFollowing() {
  this.dialog.open(UserListDialogComponent, {
    data: {
      userId: this.user.id,
      type: 'following',
      title: 'Following'
    }
  });
}
```

**Features**:
- List users with avatar, username, bio
- Follow/unfollow button (updates instantly)
- Report user button (opens report dialog)
- Click username → Navigate to profile
- Loading spinner while fetching
- Empty state if no users

---

## 🎯 Dialog Pattern

### Opening a Dialog
```typescript
// 1. Inject MatDialog in constructor
constructor(private dialog: MatDialog) {}

// 2. Open dialog with configuration
openDialog() {
  const dialogRef = this.dialog.open(MyDialogComponent, {
    width: '400px',           // Optional: Set width
    data: { key: 'value' },   // Optional: Pass data
    disableClose: true        // Optional: Prevent close on backdrop click
  });
  
  // 3. Handle dialog close
  dialogRef.afterClosed().subscribe(result => {
    if (result) {
      console.log('Dialog returned:', result);
    }
  });
}
```

### Inside Dialog Component
```typescript
export class MyDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<MyDialogComponent>,  // Reference to this dialog
    @Inject(MAT_DIALOG_DATA) public data: any           // Data passed from opener
  ) {}
  
  closeDialog() {
    this.dialogRef.close();        // Close without data
  }
  
  closeWithData() {
    this.dialogRef.close({ foo: 'bar' });  // Close with data
  }
}
```

### Dialog Template
```html
<h2 mat-dialog-title>Dialog Title</h2>

<mat-dialog-content>
  <!-- Content here -->
</mat-dialog-content>

<mat-dialog-actions align="end">
  <button mat-button mat-dialog-close>Cancel</button>
  <button mat-button (click)="closeWithData()">OK</button>
</mat-dialog-actions>
```

---

## 🎨 Angular Material Theming

### Theme Configuration (styles.scss)
```scss
@use '@angular/material' as mat;

// Define custom theme
$my-primary: mat.define-palette(mat.$indigo-palette);
$my-accent: mat.define-palette(mat.$pink-palette, A200, A100, A400);
$my-warn: mat.define-palette(mat.$red-palette);

$my-theme: mat.define-light-theme((
  color: (
    primary: $my-primary,
    accent: $my-accent,
    warn: $my-warn,
  )
));

// Apply theme
@include mat.all-component-themes($my-theme);
```

**Using Theme Colors**:
```html
<!-- Buttons -->
<button mat-raised-button color="primary">Primary</button>
<button mat-raised-button color="accent">Accent</button>
<button mat-raised-button color="warn">Warn</button>

<!-- Icons -->
<mat-icon color="primary">favorite</mat-icon>

<!-- Chips -->
<mat-chip color="accent">Chip</mat-chip>
```

---

## 📚 Summary

**Shared Module = Reusable Toolbox**

### Material Module
- Centralized Angular Material imports
- All components available everywhere
- Easy to add new Material components

### Shared Components
1. **Navbar**: Top navigation, present on all pages
   - Login/logout
   - Profile menu
   - Admin access

2. **Loading Spinner**: Visual feedback during API calls
   - Customizable message
   - Adjustable size

3. **Report Dialog**: Report inappropriate content
   - Reason selection
   - Description field
   - Submit to backend

4. **User List Dialog**: View followers/following
   - User list with avatars
   - Follow/unfollow actions
   - Report users
   - Navigate to profiles

### Key Concepts
- **@Input()**: Pass data to component
- **@Output()**: Emit events from component
- **MatDialog**: Open modal dialogs
- **MAT_DIALOG_DATA**: Pass data to dialogs
- **FormBuilder**: Create reactive forms
- **MatSnackBar**: Toast notifications
- **Subscriptions**: Listen to observable changes
- **ngOnDestroy**: Clean up to prevent memory leaks

**All components designed for reusability across the entire app!**
