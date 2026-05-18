export type ServiceUsageAuditAction =
  | "SERVICE_CREATED"
  | "SERVICE_UPDATED"
  | "SERVICE_DEACTIVATED"
  | "USAGE_CREATED"
  | "USAGE_UPDATED"
  | "USAGE_DEACTIVATED"
  | "USAGE_MIGRATED";

export interface ServiceUsageAuditResponse {
  id: number;
  serviceId?: number | null;
  serviceName?: string | null;
  usageId?: number | null;
  action: ServiceUsageAuditAction;
  oldSubject?: string | null;
  newSubject?: string | null;
  oldVersion?: number | null;
  newVersion?: number | null;
  oldRole?: "PRODUCER" | "CONSUMER" | null;
  newRole?: "PRODUCER" | "CONSUMER" | null;
  oldActive?: boolean | null;
  newActive?: boolean | null;
  changedBy?: string | null;
  createdAt: string;
}
