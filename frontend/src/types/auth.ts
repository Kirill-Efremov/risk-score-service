export type UserRole = "USER" | "ADMIN";

export interface UserResponse {
  id: number;
  username: string;
  role: UserRole;
  active: boolean;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface CurrentUserResponse {
  id: number;
  username: string;
  role: UserRole;
  active: boolean;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface RegisterRequest {
  username: string;
  password: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: "Bearer" | string;
  user: UserResponse;
}
