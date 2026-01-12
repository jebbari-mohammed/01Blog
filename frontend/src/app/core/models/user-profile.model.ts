/**
 * User Profile Model
 * 
 * Represents a user's public profile information
 */
export interface UserProfile {
  id: number;
  username: string;
  email: string;
  bio?: string;
  profilePicture?: string;
  coverImage?: string;
  createdAt: Date | string;
  
  // Statistics
  postCount: number;
  followerCount: number;
  followingCount: number;
  
  // Current user's relationship with this profile
  isFollowing?: boolean;
  isOwnProfile?: boolean;
}

/**
 * Update Profile Request
 */
export interface UpdateProfileRequest {
  username?: string;
  bio?: string;
  profilePicture?: string;
  coverImage?: string;
}
