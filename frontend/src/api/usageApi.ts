import { apiRequest } from "./client";
import type { ImpactGraph } from "../types/graph";
import type {
  CreateServiceUsageRequest,
  MigrateServiceUsageRequest,
  ServiceUsageResponse,
  UpdateServiceUsageRequest,
} from "../types/usage";

export interface ServiceUsageFilters {
  active?: boolean;
  role?: "PRODUCER" | "CONSUMER";
  subject?: string;
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

export const usageApi = {
  getServiceUsages: (serviceId: number, filters?: ServiceUsageFilters) =>
    apiRequest<ServiceUsageResponse[]>(
      withQuery(`/api/v1/services/${serviceId}/usages`, {
        active: filters?.active,
        role: filters?.role,
        subject: filters?.subject,
      }),
    ),
  createUsage: (serviceId: number, request: CreateServiceUsageRequest) =>
    apiRequest<ServiceUsageResponse>(`/api/v1/services/${serviceId}/usages`, {
      method: "POST",
      body: JSON.stringify(request),
    }),
  updateServiceUsage: (
    serviceId: number,
    usageId: number,
    request: UpdateServiceUsageRequest,
  ) =>
    apiRequest<ServiceUsageResponse>(
      `/api/v1/services/${serviceId}/usages/${usageId}`,
      {
        method: "PATCH",
        body: JSON.stringify(request),
      },
    ),
  deactivateServiceUsage: (serviceId: number, usageId: number) =>
    apiRequest<void>(`/api/v1/services/${serviceId}/usages/${usageId}`, {
      method: "DELETE",
    }),
  migrateServiceUsage: (
    serviceId: number,
    usageId: number,
    request: MigrateServiceUsageRequest,
  ) =>
    apiRequest<ServiceUsageResponse>(
      `/api/v1/services/${serviceId}/usages/${usageId}/migrate`,
      {
        method: "POST",
        body: JSON.stringify(request),
      },
    ),
  getSubjectGraph: (subject: string) =>
    apiRequest<ImpactGraph>(`/api/v1/subjects/${subject}/graph`),
};
