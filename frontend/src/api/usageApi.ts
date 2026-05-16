import { apiRequest } from "./client";
import type { ImpactGraph } from "../types/graph";
import type { ServiceResponse, ServiceUsageResponse } from "../types/usage";

export interface RegisterServiceRequest {
  name: string;
  critical: boolean;
}

export interface RegisterUsageRequest {
  subject: string;
  version?: number;
  role: "PRODUCER" | "CONSUMER";
  active?: boolean;
}

export const usageApi = {
  createService: (request: RegisterServiceRequest) =>
    apiRequest<ServiceResponse>("/api/v1/services", {
      method: "POST",
      body: JSON.stringify(request),
    }),
  createUsage: (serviceId: number, request: RegisterUsageRequest) =>
    apiRequest<ServiceUsageResponse>(`/api/v1/services/${serviceId}/usages`, {
      method: "POST",
      body: JSON.stringify(request),
    }),
  getSubjectUsages: (subject: string) =>
    apiRequest<ServiceUsageResponse[]>(`/api/v1/subjects/${subject}/usages`),
  getSubjectGraph: (subject: string) =>
    apiRequest<ImpactGraph>(`/api/v1/subjects/${subject}/graph`),
};
