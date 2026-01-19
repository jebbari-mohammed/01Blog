# 01Blog Frontend

> **Angular 17** | **Material Design** | **TypeScript** | **RxJS**

A modern, responsive blog application frontend built with Angular 17 and Material Design.

---

## 🚀 Quick Start

### Prerequisites
- Node.js 18+
- npm 9+

### Installation
```bash
cd frontend
npm install
```

### Development Server
```bash
npm start
# or
ng serve
```
Navigate to `http://localhost:4200/`. The app will automatically reload on file changes.

---

## ✨ Features

### Implemented Features
- ✅ **Authentication**
  - User registration and login
  - JWT token management
  - Auto token refresh
  - Secure logout with cleanup

- ✅ **Posts**
  - Create posts with media upload
  - Edit posts via Material Dialog
  - Delete posts (author or admin)
  - View individual posts at `/posts/:id`
  - Like/unlike posts
  - Comment on posts

- ✅ **Social Features**
  - Follow/unfollow users
  - View followers and following
  - User profiles with stats
  - User search

- ✅ **Notifications (Instagram-Inspired)**
  - Bell icon with unread badge
  - Auto-polling every 30 seconds
  - 4 types: FOLLOW, LIKE, COMMENT, NEW_POST
  - Gradient icon backgrounds
  - Mark as read (individual or all)
  - Navigate to specific posts/profiles
  - Text wrapping (no horizontal scroll)

- ✅ **Admin Dashboard**
  - View all reports
  - Update report status
  - Filter by status
  - Admin badge in navbar

- ✅ **UI/UX**
  - Responsive Material Design
  - Loading states
  - Error handling
  - Form validation
  - Smooth animations
  - Mobile-friendly

---

## 📁 Project Structure

```
frontend/src/app/
├── core/                          # Core functionality
│   ├── guards/
│   │   ├── auth.guard.ts         # Protect authenticated routes
│   │   └── admin.guard.ts        # Protect admin routes
│   ├── interceptors/
│   │   └── auth.interceptor.ts   # Inject JWT tokens
│   ├── models/
│   │   ├── auth.model.ts         # Auth types
│   │   ├── post.model.ts         # Post interface
│   │   ├── comment.model.ts      # Comment interface
│   │   ├── user.model.ts         # User interface
│   │   └── notification.model.ts # Notification types
│   └── services/
│       ├── auth.service.ts       # Authentication
│       ├── post.service.ts       # Post API calls
│       ├── comment.service.ts    # Comment API calls
│       ├── like.service.ts       # Like API calls
│       ├── subscription.service.ts # Follow API calls
│       ├── user.service.ts       # User API calls
│       ├── report.service.ts     # Report API calls
│       └── notification.service.ts # Notification API + polling
│
├── features/                      # Feature modules
│   ├── auth/
│   │   ├── login/               # Login component
│   │   └── register/            # Register component
│   ├── home/
│   │   └── home.component.*     # Main feed
│   ├── post/
│   │   ├── post-card/           # Post display
│   │   ├── create-post/         # Post creation
│   │   ├── edit-post-dialog/    # Post editing dialog
│   │   ├── post-detail/         # Individual post view
│   │   └── comments/            # Comments component
│   ├── profile/
│   │   └── profile.component.*  # User profile
│   ├── admin/
│   │   └── admin-dashboard/     # Admin panel
│   └── user/
│       └── user-card/           # User display
│
├── shared/                        # Shared components
│   ├── components/
│   │   ├── navbar/              # Navigation with notifications
│   │   └── notification-panel/  # Notification dropdown
│   └── material.module.ts       # Material imports
│
├── app.component.*               # Root component
├── app.routes.ts                 # Route configuration
└── app.config.ts                 # App configuration
```

---

## 🎯 Key Components

### Authentication
- **LoginComponent**: User login form
- **RegisterComponent**: User registration
- **AuthGuard**: Protects authenticated routes
- **AdminGuard**: Protects admin-only routes
- **AuthInterceptor**: Automatically adds JWT to requests

### Posts
- **HomeComponent**: Main feed with all posts
- **PostCardComponent**: Individual post display with actions
- **CreatePostComponent**: Post creation form
- **EditPostDialogComponent**: Material Dialog for editing
- **PostDetailComponent**: View individual post at `/posts/:id`
- **CommentsComponent**: Comment list and creation

### Social
- **ProfileComponent**: User profile with stats
- **FollowersDialogComponent**: Followers/following lists
- **UserCardComponent**: User display in lists

### Notifications
- **NotificationPanelComponent**: Instagram-inspired dropdown
  - 480px wide panel
  - Gradient icon backgrounds
  - Auto-polling every 30 seconds
  - Navigate to specific content

### Admin
- **AdminDashboardComponent**: Report management
- **UpdateReportStatusDialogComponent**: Status updates

---

## 🔧 Services

### AuthService
```typescript
- register(request): Observable<AuthResponse>
- login(request): Observable<AuthResponse>
- logout(): void
- isLoggedIn(): boolean
- getCurrentUser(): User | null
- currentUser$: BehaviorSubject<User | null>
```

### PostService
```typescript
- getAllPosts(params): Observable<Page<Post>>
- getPostById(id): Observable<Post>  // NEW
- createPost(request): Observable<Post>
- updatePost(id, request): Observable<Post>
- deletePost(id): Observable<void>
```

### NotificationService
```typescript
- getMyNotifications(): Observable<Notification[]>
- getUnreadNotifications(): Observable<Notification[]>
- getUnreadCount(): Observable<number>
- markAsRead(id): Observable<void>
- markAllAsRead(): Observable<void>
```

---

## 🛣️ Routes

### Public Routes
```typescript
/auth/login              # Login page
/auth/register           # Registration page
/home                    # Main feed (requires auth)
/posts/:id               # Individual post view
```

### Protected Routes (Requires Authentication)
```typescript
/profile                 # Current user profile
/profile/:username       # Other user profile
/profile/edit            # Edit profile
/post/create             # Create new post
```

### Admin Routes (Requires ADMIN role)
```typescript
/admin/dashboard         # Admin panel
```

---

## 🎨 Styling

### Material Theme
- Primary: Blue (#1976d2)
- Accent: Pink
- Custom SCSS variables
- Responsive breakpoints

### Component Styles
- SCSS with BEM naming
- Component-scoped styles
- Global styles in `styles.scss`
- Material theme customization

---

## 🔐 Security

### Authentication
- JWT tokens stored in localStorage
- Auto token injection via interceptor
- Token validation on app init
- Secure logout with cleanup

### Route Protection
- AuthGuard for authenticated routes
- AdminGuard for admin routes
- Automatic redirect to login
- Role-based access control

---

## 🧪 Testing

### Manual Testing
1. Start dev server: `npm start`
2. Navigate to `http://localhost:4200`
3. Register a new user
4. Test all features:
   - Create posts
   - Like and comment
   - Follow users
   - Check notifications
   - Edit posts
   - Admin dashboard (if admin)

### Test Admin User
Update user role in database:
```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'your@email.com';
```

---

## 📦 Build

### Development Build
```bash
npm run build
```

### Production Build
```bash
npm run build --configuration production
```

Build artifacts will be in `dist/` directory.

### Bundle Size
- Initial: 497.66 kB
- Total: 1.11 MB (including notifications)

---

## 🐛 Troubleshooting

### Port Already in Use
```bash
# Kill process on port 4200
lsof -ti:4200 | xargs kill -9
```

### Module Not Found
```bash
# Reinstall dependencies
rm -rf node_modules package-lock.json
npm install
```

### Build Errors
```bash
# Clear Angular cache
rm -rf .angular
ng serve
```

---

## 📚 Technologies

### Framework & Core
- **Angular 17** - Latest Angular version
- **TypeScript 5** - Type-safe development
- **RxJS 7** - Reactive programming

### UI Components
- **Angular Material 17** - Material Design components
- **Material Icons** - Icon library
- **SCSS** - Advanced styling

### HTTP & State
- **HttpClient** - HTTP requests
- **Interceptors** - Request/response handling
- **BehaviorSubject** - State management

### Routing & Guards
- **Angular Router** - Navigation
- **Route Guards** - Access control
- **Lazy Loading** - Performance optimization

---

## 🎓 Learning Resources

### Angular Documentation
- [Angular Docs](https://angular.io/docs)
- [Angular Material](https://material.angular.io)
- [RxJS Docs](https://rxjs.dev)

### Project Documentation
- `08_FRONTEND_CORE.md` - Core module details
- `09_FRONTEND_FEATURES.md` - Feature components
- `10_FRONTEND_SHARED.md` - Shared components

---

## 📊 Statistics

- **Components:** 25+
- **Services:** 8
- **Guards:** 2
- **Interceptors:** 1
- **Routes:** 15+
- **Lines of Code:** ~8,000+

---

## 🚀 Deployment

### Environment Configuration
Update `src/environments/`:
- `environment.ts` - Development
- `environment.prod.ts` - Production

### Build for Production
```bash
npm run build --configuration production
```

### Deploy
Deploy the `dist/` folder to:
- Firebase Hosting
- Netlify
- Vercel
- AWS S3 + CloudFront
- Any static hosting

---

## 📝 Code Scaffolding

Generate new components:
```bash
# Component
ng generate component features/my-feature

# Service
ng generate service core/services/my-service

# Guard
ng generate guard core/guards/my-guard

# Model (interface)
ng generate interface core/models/my-model
```

---

## 🤝 Contributing

1. Follow Angular style guide
2. Use TypeScript strict mode
3. Write component-scoped SCSS
4. Document complex logic
5. Handle errors properly
6. Add loading states
7. Test before committing

---

## 📄 License

Educational project for learning purposes.

---

## 👤 Author

**Jebbari Mohammed**
- GitHub: [@jebbari-mohammed](https://github.com/jebbari-mohammed)

---

**Built with ❤️ using Angular 17**
