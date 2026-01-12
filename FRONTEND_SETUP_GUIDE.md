# 🎨 Angular Frontend Setup Guide - 01Blog

## 📋 Prerequisites

Before starting, make sure you have installed:
- **Node.js** (v18 or higher) - [Download here](https://nodejs.org/)
- **npm** (comes with Node.js)
- **Angular CLI** - Install globally:
  ```bash
  npm install -g @angular/cli
  ```

Check versions:
```bash
node --version    # Should be v18+
npm --version     # Should be 9+
ng version        # Should be 17+
```

---

## 🚀 Step 1: Create Angular Project

Navigate to your project root and create the Angular app:

```bash
cd /Users/jebbarimohammed/Downloads/01Blog

# Create Angular project with routing and SCSS
ng new frontend --routing --style=scss --skip-git

# Navigate to frontend folder
cd frontend
```

**Questions you'll be asked:**
- `Would you like to add Angular routing?` → **Yes**
- `Which stylesheet format would you like to use?` → **SCSS**

---

## 🎨 Step 2: Install Angular Material

Angular Material provides pre-built, beautiful UI components:

```bash
ng add @angular/material
```

**Select:**
- Theme: **Indigo/Pink** (or your preference)
- Set up global typography: **Yes**
- Include browser animations: **Yes**

---

## 📦 Step 3: Install Additional Dependencies

```bash
# HTTP Client for API calls
# (Already included in Angular, just need to import)

# JWT handling
npm install @auth0/angular-jwt

# Forms and validation
# (Already included in Angular)

# Icons
npm install @angular/material-icons
```

---

## 📁 Step 4: Project Structure

Your frontend folder will have this structure:

```
frontend/
├── src/
│   ├── app/
│   │   ├── core/                    # Core services, guards, interceptors
│   │   │   ├── services/
│   │   │   │   ├── auth.service.ts
│   │   │   │   ├── post.service.ts
│   │   │   │   ├── user.service.ts
│   │   │   │   ├── like.service.ts
│   │   │   │   ├── comment.service.ts
│   │   │   │   └── subscription.service.ts
│   │   │   ├── guards/
│   │   │   │   └── auth.guard.ts
│   │   │   ├── interceptors/
│   │   │   │   └── auth.interceptor.ts
│   │   │   └── models/
│   │   │       ├── user.model.ts
│   │   │       ├── post.model.ts
│   │   │       ├── comment.model.ts
│   │   │       └── response.model.ts
│   │   │
│   │   ├── features/                # Feature modules
│   │   │   ├── auth/
│   │   │   │   ├── login/
│   │   │   │   ├── register/
│   │   │   │   └── auth-routing.module.ts
│   │   │   │
│   │   │   ├── home/
│   │   │   │   ├── home.component.ts
│   │   │   │   └── home.component.html
│   │   │   │
│   │   │   ├── profile/
│   │   │   │   ├── profile.component.ts
│   │   │   │   └── profile.component.html
│   │   │   │
│   │   │   ├── post/
│   │   │   │   ├── post-create/
│   │   │   │   ├── post-list/
│   │   │   │   └── post-card/
│   │   │   │
│   │   │   └── admin/
│   │   │       └── dashboard/
│   │   │
│   │   ├── shared/                  # Shared components
│   │   │   ├── components/
│   │   │   │   ├── navbar/
│   │   │   │   ├── footer/
│   │   │   │   └── loading-spinner/
│   │   │   └── material.module.ts
│   │   │
│   │   ├── app.component.ts
│   │   ├── app.component.html
│   │   ├── app-routing.module.ts
│   │   └── app.module.ts
│   │
│   ├── assets/                      # Images, fonts, etc.
│   ├── environments/                # Environment configs
│   │   ├── environment.ts
│   │   └── environment.prod.ts
│   ├── styles.scss                  # Global styles
│   └── index.html
│
├── angular.json
├── package.json
└── tsconfig.json
```

---

## 🔧 Step 5: Configure Environment

Update `src/environments/environment.ts`:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

Update `src/environments/environment.prod.ts`:

```typescript
export const environment = {
  production: true,
  apiUrl: 'https://your-production-api.com/api'
};
```

---

## 📝 Step 6: Generate Core Structure

Run these commands to generate the structure:

```bash
# Core services
ng generate service core/services/auth
ng generate service core/services/post
ng generate service core/services/user
ng generate service core/services/like
ng generate service core/services/comment
ng generate service core/services/subscription

# Guards
ng generate guard core/guards/auth

# Interceptors
ng generate interceptor core/interceptors/auth

# Models (interfaces)
ng generate interface core/models/user
ng generate interface core/models/post
ng generate interface core/models/comment
ng generate interface core/models/response

# Shared components
ng generate component shared/components/navbar
ng generate component shared/components/footer
ng generate component shared/components/loading-spinner

# Auth feature
ng generate module features/auth --routing
ng generate component features/auth/login
ng generate component features/auth/register

# Home feature
ng generate component features/home

# Profile feature
ng generate component features/profile

# Post feature
ng generate component features/post/post-create
ng generate component features/post/post-list
ng generate component features/post/post-card

# Material module
ng generate module shared/material
```

---

## 🎯 Step 7: Configure Material Module

Create `src/app/shared/material.module.ts`:

```typescript
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialogModule } from '@angular/material/dialog';
import { MatChipsModule } from '@angular/material/chips';
import { MatBadgeModule } from '@angular/material/badge';
import { MatTooltipModule } from '@angular/material/tooltip';

const materialModules = [
  MatButtonModule,
  MatCardModule,
  MatFormFieldModule,
  MatInputModule,
  MatToolbarModule,
  MatIconModule,
  MatMenuModule,
  MatSidenavModule,
  MatListModule,
  MatGridListModule,
  MatSelectModule,
  MatProgressSpinnerModule,
  MatSnackBarModule,
  MatDialogModule,
  MatChipsModule,
  MatBadgeModule,
  MatTooltipModule
];

@NgModule({
  imports: materialModules,
  exports: materialModules
})
export class MaterialModule { }
```

---

## 🔐 Step 8: Setup Authentication Service

I'll provide the complete code for each service in the next steps.

---

## ▶️ Step 9: Run the Application

```bash
# Development server
ng serve

# Or with custom port
ng serve --port 4200

# Open in browser
# http://localhost:4200
```

---

## 📦 Step 10: Build for Production

```bash
# Build
ng build --configuration production

# Output will be in dist/frontend/
```

---

## 🎨 Step 11: Custom Theming (Optional)

Create `src/styles.scss`:

```scss
@import '@angular/material/prebuilt-themes/indigo-pink.css';

// Custom theme colors
$primary: #3f51b5;
$accent: #ff4081;
$warn: #f44336;

// Global styles
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: Roboto, "Helvetica Neue", sans-serif;
  background-color: #f5f5f5;
}

// Custom classes
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.card-elevated {
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transition: all 0.3s ease;
  
  &:hover {
    box-shadow: 0 4px 16px rgba(0,0,0,0.2);
  }
}
```

---

## ✅ Verification Checklist

After setup, verify:
- [ ] `ng serve` runs without errors
- [ ] Browser opens at http://localhost:4200
- [ ] Angular Material theme is visible
- [ ] All folders are created
- [ ] No compilation errors

---

## 🐛 Common Issues & Solutions

### Issue 1: "ng: command not found"
```bash
npm install -g @angular/cli
```

### Issue 2: Port 4200 already in use
```bash
ng serve --port 4300
```

### Issue 3: Module not found errors
```bash
npm install
```

### Issue 4: CORS errors when calling backend
Add this to Spring Boot `SecurityConfiguration.java`:
```java
@Bean
public CorsFilter corsFilter() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.addAllowedOrigin("http://localhost:4200");
    config.addAllowedHeader("*");
    config.addAllowedMethod("*");
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    
    return new CorsFilter(source);
}
```

---

## 📚 Next Steps

After completing this setup:
1. ✅ **Models & Interfaces** - Define TypeScript interfaces
2. ✅ **Auth Service** - Handle login/register/JWT
3. ✅ **HTTP Interceptor** - Auto-add JWT to requests
4. ✅ **Auth Guard** - Protect routes
5. ✅ **Login/Register Pages** - Create forms
6. ✅ **Navbar Component** - Navigation bar
7. ✅ **Home Page** - Post feed
8. ✅ **Profile Page** - User profiles
9. ✅ **Post Components** - Create/list/card

---

## 🚀 Ready to Start?

Run these commands in order:

```bash
# 1. Create project
cd /Users/jebbarimohammed/Downloads/01Blog
ng new frontend --routing --style=scss --skip-git

# 2. Install Material
cd frontend
ng add @angular/material

# 3. Install dependencies
npm install @auth0/angular-jwt

# 4. Start development
ng serve
```

**Once you've run these commands, let me know and I'll provide the code for each component!** 🎨
