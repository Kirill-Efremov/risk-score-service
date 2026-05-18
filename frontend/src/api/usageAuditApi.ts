import { apiRequest } from "./client";
import type {
  ServiceUsageAuditAction,
  ServiceUsageAuditResponse,
} from "../types/usageAudit";

export interface UsageAuditFilters {
  serviceId?: number;
  usageId?: number;
  action?: ServiceUsageAuditAction;
  limit?: number;
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

export const usageAuditApi = {
  getServiceAudit: (serviceId: number, filters?: UsageAuditFilters) =>
    apiRequest<ServiceUsageAuditResponse[]>(
      withQuery(`/api/v1/services/${serviceId}/audit`, {
        action: filters?.action,
        limit: filters?.limit,
      }),
    ),
  getUsageAudit: (serviceId: number, usageId: number, filters?: UsageAuditFilters) =>
    apiRequest<ServiceUsageAuditResponse[]>(
      withQuery(`/api/v1/services/${serviceId}/usages/${usageId}/audit`, {
        limit: filters?.limit,
      }),
    ),
  getUsageAuditLog: (filters?: UsageAuditFilters) =>
    apiRequest<ServiceUsageAuditResponse[]>(
      withQuery("/api/v1/usage-audit", {
        serviceId: filters?.serviceId,
        usageId: filters?.usageId,
        action: filters?.action,
        limit: filters?.limit,
      }),
    ),
};
