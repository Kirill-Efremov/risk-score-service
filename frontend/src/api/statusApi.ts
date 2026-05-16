import { apiRequest } from "./client";
import type { SystemStatusResponse } from "../types/common";

export const statusApi = {
  getStatus: () => apiRequest<SystemStatusResponse>("/api/v1/status"),
};
