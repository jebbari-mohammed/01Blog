// User Model - Represents a user in our system
// This matches the User entity from Spring Boot backend

export interface User {
  id: number;
  username: string;
  email: string;
  role: 'USER' | 'ADMIN';
  bio?: string;
  profilePicture?: string;
  coverImage?: string;
  createdAt: string;
}

// User Profile Response - What we get when viewing a profile
export interface UserProfileResponse {
  id: number;
  username: string;
  email: string;
  bio: string;
  profilePicture: string;
  coverImage: string;
  followersCount: number;
  followingCount: number;
  postsCount: number;
  createdAt: string;
}

// Update Profile Request - What we send to update profile
export interface UpdateProfileRequest {
  username?: string;
  bio?: string;
  profilePicture?: string;
  coverImage?: string;
}
