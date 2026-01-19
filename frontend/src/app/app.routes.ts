import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login.component';
import { RegisterComponent } from './features/auth/register/register.component';
import { HomeComponent } from './features/home/home.component';
import { ProfileComponent } from './features/user/profile/profile.component';
import { EditProfileComponent } from './features/user/edit-profile/edit-profile.component';
import { CreatePostComponent } from './features/post/create-post/create-post.component';
import { PostDetailComponent } from './features/post/post-detail/post-detail.component';
import { AdminDashboardComponent } from './features/admin/admin-dashboard/admin-dashboard.component';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';

/**
 * Routes Configuration - Defines all pages in the app
 * 
 * How routes work:
 * - User types URL in browser (e.g., localhost:4200/home)
 * - Angular looks at this routes array
 * - Finds matching path
 * - Loads the corresponding component
 * 
 * Protected routes:
 * - Routes with canActivate: [authGuard] require login
 * - If not logged in, user is redirected to /auth/login
 */

export const routes: Routes = [
  // Default route - redirect to home
  {
    path: '',
    redirectTo: 'home',
    pathMatch: 'full'
  },

  // Authentication routes (public - anyone can access)
  {
    path: 'auth/login',
    component: LoginComponent
  },
  {
    path: 'auth/register',
    component: RegisterComponent
  },

  // Home page (protected - must be logged in)
  {
    path: 'home',
    component: HomeComponent,
    canActivate: [authGuard]  // <-- Requires login
  },

  // Create post (protected - must be logged in)
  {
    path: 'post/create',
    component: CreatePostComponent,
    canActivate: [authGuard]  // <-- Requires login
  },

  // View single post (public - anyone can view)
  {
    path: 'posts/:id',
    component: PostDetailComponent
  },

  // Edit profile (protected - must be logged in)
  // IMPORTANT: This MUST come BEFORE 'profile/:username' 
  // Otherwise 'edit' will be treated as a username
  {
    path: 'profile/edit',
    component: EditProfileComponent,
    canActivate: [authGuard]  // <-- Requires login
  },

  // Profile page (protected - must be logged in)
  // This dynamic route MUST come after specific routes like 'profile/edit'
  {
    path: 'profile/:username',  // :username is a route parameter
    component: ProfileComponent,
    canActivate: [authGuard]  // <-- Requires login
  },

  // Admin Dashboard (protected - ADMIN role only)
  {
    path: 'admin',
    component: AdminDashboardComponent,
    canActivate: [adminGuard]  // <-- Requires ADMIN role
  },

  // Wildcard route - handles unknown URLs
  {
    path: '**',
    redirectTo: 'home'
  }
];
