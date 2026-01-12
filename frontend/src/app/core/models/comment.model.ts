// Comment Model - Represents a comment on a post
// This matches the Comment entity from Spring Boot backend

export interface Comment {
  id: number;
  text: string;
  username: string;
  postId: number;
  createdAt: string;
  updatedAt?: string;
}

// Create Comment Request - What we send to add a comment
export interface CreateCommentRequest {
  text: string;
}

// Update Comment Request - What we send to update a comment
export interface UpdateCommentRequest {
  text: string;
}
