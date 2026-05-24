import { apiRequest } from "./client";
import type {
  AuthResponse,
  CurrentUserResponse,
  LoginRequest,
  RegisterRequest,
  UserResponse,
} from "../types/auth";

export const authApi = {
  register: (request: RegisterRequest) =>
    apiRequest<UserResponse>("/api/v1/auth/register", {
      method: "POST",
      body: JSON.stringify(request),
    }),
  login: (request: LoginRequest) =>
    apiRequest<AuthResponse>("/api/v1/auth/login", {
      method: "POST",
      body: JSON.stringify(request),
    }),
  me: () => apiRequest<CurrentUserResponse>("/api/v1/auth/me"),
};
