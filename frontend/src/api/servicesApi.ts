import { apiRequest } from "./client";
import type {
  CreateServiceRequest,
  ServiceResponse,
  UpdateServiceRequest,
} from "../types/usage";

export interface ServicesFilters {
  active?: boolean;
  critical?: boolean;
}

function withQuery(
  path: string,
  params: Record<string, string | number | boolean | undefined>,
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

export const servicesApi = {
  getServices: (filters?: ServicesFilters) =>
    apiRequest<ServiceResponse[]>(
      withQuery("/api/v1/services", {
        active: filters?.active,
        critical: filters?.critical,
      }),
    ),
  getService: (serviceId: number) =>
    apiRequest<ServiceResponse>(`/api/v1/services/${serviceId}`),
  createService: (request: CreateServiceRequest) =>
    apiRequest<ServiceResponse>("/api/v1/services", {
      method: "POST",
      body: JSON.stringify(request),
    }),
  updateService: (serviceId: number, request: UpdateServiceRequest) =>
    apiRequest<ServiceResponse>(`/api/v1/services/${serviceId}`, {
      method: "PATCH",
      body: JSON.stringify(request),
    }),
  deactivateService: (serviceId: number) =>
    apiRequest<void>(`/api/v1/services/${serviceId}`, {
      method: "DELETE",
    }),
};
