import { apiRequest } from "./client";
import type { UserResponse, UserRole } from "../types/auth";

interface GetUsersFilters {
  role?: UserRole;
  active?: boolean;
}

interface UpdateUserRequest {
  role?: UserRole;
  active?: boolean;
}

function withQuery(
  path: string,
  params: Record<string, string | boolean | undefined>,
) {
  const query = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== "") {
      query.set(key, String(value));
    }
  });
  const queryString = query.toString();
  return queryString ? `${path}?${queryString}` : path;
}

export const adminUsersApi = {
  getUsers: (filters?: GetUsersFilters) =>
    apiRequest<UserResponse[]>(
      withQuery("/api/v1/admin/users", {
        role: filters?.role,
        active: filters?.active,
      }),
    ),
  updateUser: (userId: number, request: UpdateUserRequest) =>
    apiRequest<UserResponse>(`/api/v1/admin/users/${userId}`, {
      method: "PATCH",
      body: JSON.stringify(request),
    }),
  deactivateUser: (userId: number) =>
    apiRequest<UserResponse>(`/api/v1/admin/users/${userId}`, {
      method: "DELETE",
    }),
};
