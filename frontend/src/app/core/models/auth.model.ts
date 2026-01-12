// Authentication Models
// These match the DTOs from Spring Boot AuthController

// Register Request - What we send to /api/auth/register
export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

// Login Request - What we send to /api/auth/login
export interface LoginRequest {
  email: string;
  password: string;
}

// Authentication Response - What we get back after login/register
// IMPORTANT: Backend returns "token" (not "accessToken")
export interface AuthenticationResponse {
  token: string;              // Backend field name
  refreshToken: string;
  message?: string;           // Optional message field
}

// Refresh Token Request - What we send to /api/auth/refresh
export interface RefreshTokenRequest {
  refreshToken: string;
}

// API Error Response - Standard error format from Spring Boot
export interface ApiErrorResponse {
  message: string;
  status: number;
  timestamp: string;
}

// Generic API Response - For simple success messages
export interface ApiResponse {
  message: string;
}
