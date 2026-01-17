# 01Blog - Full Stack Blog Application

## 📖 Project Overview

This is a **Full Stack Blog Application** built with:
- **Backend**: Spring Boot (Java) - REST API
- **Frontend**: Angular 17 - Single Page Application (SPA)
- **Database**: PostgreSQL - Relational Database
- **Authentication**: JWT (JSON Web Tokens)

## 🏗️ Architecture

```
01Blog/
├── backend (Spring Boot)
│   ├── Controllers - Handle HTTP requests
│   ├── Services - Business logic
│   ├── Repositories - Database access
│   ├── Models - Database entities
│   ├── DTOs - Data Transfer Objects
│   └── Config - Security, JWT, CORS
│
└── frontend (Angular)
    ├── Core - Services, Models, Guards, Interceptors
    ├── Features - Page components
    ├── Shared - Reusable components
    └── Assets - Images, styles
```

## ✨ Features

### User Features
- ✅ Register & Login with JWT authentication
- ✅ Create, edit, delete posts
- ✅ Like/unlike posts
- ✅ Comment on posts
- ✅ Follow/unfollow users
- ✅ View user profiles
- ✅ Report posts, comments, and users
- ✅ Upload images for posts and profile pictures

### Admin Features
- ✅ View all reports (posts, comments, users)
- ✅ Filter reports by status (Pending, Reviewed, Resolved, Dismissed)
- ✅ Update report status with admin notes
- ✅ Real-time pending reports count badge

## 🚀 Getting Started

### Prerequisites
- Java 17 or higher
- Node.js 18 or higher
- PostgreSQL 14 or higher
- Maven (included with mvnw)

### Backend Setup

1. **Create Database**
```bash
psql postgres -c "CREATE DATABASE blog_db;"
```

2. **Configure Database** (src/main/resources/application.properties)
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/blog_db
spring.datasource.username=your_username
spring.datasource.password=your_password
```

3. **Run Backend**
```bash
./mvnw spring-boot:run
```
Backend runs on: http://localhost:8080

4. **Create Admin User**
```bash
curl -X POST http://localhost:8080/api/auth/register-admin \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "email": "admin@blog.com",
    "password": "admin123"
  }'
```

### Frontend Setup

1. **Install Dependencies**
```bash
cd frontend
npm install
```

2. **Run Frontend**
```bash
npm start
```
Frontend runs on: http://localhost:4200

## 📁 Project Structure Explained

### Backend (Spring Boot)

#### **Models** (`src/main/java/com/_Blog/mojebbari/models/`)
- Database entities (tables)
- Represent real-world objects: User, Post, Comment, Like, etc.

#### **Repositories** (`src/main/java/com/_Blog/mojebbari/repositories/`)
- Interface with database
- Provide methods to query data

#### **Services** (`src/main/java/com/_Blog/mojebbari/services/`)
- Business logic
- Process data, validate rules

#### **Controllers** (`src/main/java/com/_Blog/mojebbari/controllers/`)
- Handle HTTP requests
- Define API endpoints

#### **DTOs** (`src/main/java/com/_Blog/mojebbari/dto/`)
- Data Transfer Objects
- Define what data is sent/received

#### **Config** (`src/main/java/com/_Blog/mojebbari/config/`)
- Application configuration
- Security, JWT, CORS, Exception handling

### Frontend (Angular)

#### **Core** (`frontend/src/app/core/`)
- **Services**: API communication
- **Models**: TypeScript interfaces
- **Guards**: Route protection
- **Interceptors**: Add JWT to requests

#### **Features** (`frontend/src/app/features/`)
- Page components
- Each feature is a module (auth, post, profile, admin)

#### **Shared** (`frontend/src/app/shared/`)
- Reusable components
- Used across multiple features

## 🔐 Authentication Flow

1. User registers → Backend creates user → Returns JWT token
2. User logs in → Backend validates credentials → Returns JWT token
3. Frontend stores token in localStorage
4. Every API request includes token in Authorization header
5. Backend validates token before processing request

## 📊 Database Schema

### Tables
- **_user**: User accounts
- **posts**: Blog posts
- **comments**: Comments on posts
- **likes**: User likes on posts
- **subscriptions**: User follow relationships
- **reports**: User-reported content

### Relationships
- User → Posts (one-to-many)
- User → Comments (one-to-many)
- Post → Comments (one-to-many)
- Post → Likes (one-to-many)
- User → Subscriptions (many-to-many)

## 🛠️ Technologies Used

### Backend
- **Spring Boot 3.5.7** - Java framework
- **Spring Security** - Authentication & Authorization
- **JWT (jjwt)** - Token-based auth
- **PostgreSQL** - Database
- **Lombok** - Reduce boilerplate code
- **Hibernate** - ORM (Object-Relational Mapping)

### Frontend
- **Angular 17** - Frontend framework
- **Angular Material** - UI components
- **RxJS** - Reactive programming
- **TypeScript** - Type-safe JavaScript

## 📖 API Documentation

Base URL: `http://localhost:8080/api`

### Authentication
- `POST /auth/register` - Register new user
- `POST /auth/register-admin` - Register admin user
- `POST /auth/login` - Login user
- `POST /auth/refresh` - Refresh access token

### Posts
- `GET /posts` - Get all posts
- `GET /posts/{id}` - Get post by ID
- `POST /posts` - Create new post
- `PUT /posts/{id}` - Update post
- `DELETE /posts/{id}` - Delete post

### Comments
- `GET /comments/post/{postId}` - Get comments for post
- `POST /comments` - Create comment
- `PUT /comments/{id}` - Update comment
- `DELETE /comments/{id}` - Delete comment

### Likes
- `POST /likes/post/{postId}` - Like/unlike post
- `GET /likes/post/{postId}` - Check if user liked post

### Users
- `GET /users/profile/{username}` - Get user profile
- `PUT /users/profile` - Update own profile
- `POST /users/profile-picture` - Upload profile picture

### Subscriptions
- `POST /subscriptions/follow/{userId}` - Follow user
- `POST /subscriptions/unfollow/{userId}` - Unfollow user
- `GET /subscriptions/followers/{userId}` - Get followers
- `GET /subscriptions/following/{userId}` - Get following

### Reports (Admin)
- `POST /reports` - Create report
- `GET /reports` - Get all reports (admin only)
- `PUT /reports/{id}/status` - Update report status (admin only)
- `GET /reports/pending/count` - Get pending reports count

## 🤝 Contributing

This is your first project! Feel free to:
- Add new features
- Fix bugs
- Improve documentation
- Refactor code

## 📝 License

This project is for learning purposes.

## 🎓 Learning Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Angular Documentation](https://angular.io/docs)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [JWT Introduction](https://jwt.io/introduction)

## 🐛 Troubleshooting

### Backend won't start
- Check if PostgreSQL is running
- Verify database credentials in application.properties
- Check if port 8080 is available

### Frontend won't start
- Run `npm install` first
- Check if port 4200 is available
- Clear npm cache: `npm cache clean --force`

### Can't login
- Verify backend is running
- Check browser console for errors
- Verify JWT token is being sent in requests

## 📞 Support

For questions about this codebase, refer to the individual README files in each directory for detailed explanations of every file.
