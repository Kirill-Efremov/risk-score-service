import type { ApiErrorResponse } from "../types/common";

export const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
export const ACCESS_TOKEN_STORAGE_KEY = "risk-score-access-token";
export const AUTH_UNAUTHORIZED_EVENT = "auth:unauthorized";

export class ApiError extends Error {
  payload?: ApiErrorResponse;
  status?: number;
  errorCode?: string;
  details: ApiErrorResponse["details"];

  constructor(message: string, payload?: ApiErrorResponse) {
    super(message);
    this.name = "ApiError";
    this.payload = payload;
    this.status = payload?.status;
    this.errorCode = payload?.errorCode;
    this.details = payload?.details ?? [];
  }
}

export function getStoredAccessToken() {
  return window.localStorage.getItem(ACCESS_TOKEN_STORAGE_KEY);
}

export function setStoredAccessToken(token: string) {
  window.localStorage.setItem(ACCESS_TOKEN_STORAGE_KEY, token);
}

export function clearStoredAccessToken() {
  window.localStorage.removeItem(ACCESS_TOKEN_STORAGE_KEY);
}

export async function apiRequest<T>(
  path: string,
  init?: RequestInit,
): Promise<T> {
  const accessToken = getStoredAccessToken();
  const headers = new Headers(init?.headers ?? {});

  if (!headers.has("Content-Type") && init?.body !== undefined) {
    headers.set("Content-Type", "application/json");
  }

  if (accessToken && !headers.has("Authorization")) {
    headers.set("Authorization", `Bearer ${accessToken}`);
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...init,
    headers,
  });

  if (!response.ok) {
    let payload: ApiErrorResponse | undefined;
    try {
      payload = (await response.json()) as ApiErrorResponse;
    } catch {
      payload = undefined;
    }

    if (response.status === 401) {
      clearStoredAccessToken();
      window.dispatchEvent(
        new CustomEvent(AUTH_UNAUTHORIZED_EVENT, {
          detail: payload,
        }),
      );
    }

    throw new ApiError(
      payload?.message || `${response.status} ${response.statusText}`,
      payload ?? {
        timestamp: new Date().toISOString(),
        status: response.status,
        errorCode: "HTTP_ERROR",
        message: `${response.status} ${response.statusText}`,
        path,
        details: [],
      },
    );
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return (await response.json()) as T;
}
