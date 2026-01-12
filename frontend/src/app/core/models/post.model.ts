// Post Model - Represents a blog post
// This matches the PostResponse DTO from Spring Boot backend

export interface Post {
  id: number;
  title: string;
  content: string;
  category?: string;
  mediaUrl?: string;
  authorUsername: string;  // Backend sends authorUsername, not username
  authorId: number;
  createdAt: string;
  likeCount: number;
  commentCount: number;
  isLikedByCurrentUser: boolean;
}

// Create Post Request - What we send to create a new post
export interface CreatePostRequest {
  title: string;
  content: string;
  category?: string;
  mediaUrl?: string;
}

// Update Post Request - What we send to update a post
export interface UpdatePostRequest {
  title?: string;
  content?: string;
  category?: string;
  mediaUrl?: string;
}

// Paginated Posts Response - What we get when fetching posts with pagination
export interface PaginatedPostsResponse {
  content: Post[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
}
