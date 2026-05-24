import { apiRequest } from "./client";
import type {
  ApprovalDecisionRequest,
  SchemaApprovalResponse,
  SchemaApprovalStatus,
} from "../types/approval";

interface ApprovalFilters {
  status?: SchemaApprovalStatus;
  subject?: string;
  requestedBy?: string;
  limit?: number;
}

function buildQuery(filters?: ApprovalFilters) {
  const params = new URLSearchParams();

  if (filters?.status) {
    params.set("status", filters.status);
  }
  if (filters?.subject) {
    params.set("subject", filters.subject);
  }
  if (filters?.requestedBy) {
    params.set("requestedBy", filters.requestedBy);
  }
  if (filters?.limit) {
    params.set("limit", String(filters.limit));
  }

  const query = params.toString();
  return query ? `?${query}` : "";
}

export const schemaApprovalsApi = {
  getMyApprovals: (filters?: Pick<ApprovalFilters, "status" | "limit">) =>
    apiRequest<SchemaApprovalResponse[]>(
      `/api/v1/schema-approvals/my${buildQuery(filters)}`,
    ),

  getApproval: (id: number) =>
    apiRequest<SchemaApprovalResponse>(`/api/v1/schema-approvals/${id}`),

  getAdminApprovals: (filters?: ApprovalFilters) =>
    apiRequest<SchemaApprovalResponse[]>(
      `/api/v1/admin/schema-approvals${buildQuery(filters)}`,
    ),

  getAdminApproval: (id: number) =>
    apiRequest<SchemaApprovalResponse>(`/api/v1/admin/schema-approvals/${id}`),

  approveApproval: (id: number, request: ApprovalDecisionRequest) =>
    apiRequest<SchemaApprovalResponse>(
      `/api/v1/admin/schema-approvals/${id}/approve`,
      {
        method: "POST",
        body: JSON.stringify(request),
      },
    ),

  rejectApproval: (id: number, request: ApprovalDecisionRequest) =>
    apiRequest<SchemaApprovalResponse>(
      `/api/v1/admin/schema-approvals/${id}/reject`,
      {
        method: "POST",
        body: JSON.stringify(request),
      },
    ),
};
