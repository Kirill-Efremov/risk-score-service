export type SchemaApprovalStatus =
  | "PENDING"
  | "REJECTED"
  | "PUBLISHED"
  | "REGISTRY_REJECTED"
  | "CANCELLED";

export interface SchemaApprovalResponse {
  id: number;
  subject: string;
  schemaType: string;
  compatibilityMode?: string | null;
  oldVersion?: number | null;
  newSchemaText: string;
  analysisId?: number | null;
  formalCompatible: boolean;
  governanceDecision: string;
  riskScore: number;
  riskLevel: string;
  status: SchemaApprovalStatus;
  requestedBy?: string | null;
  requestedAt: string;
  reviewedBy?: string | null;
  reviewedAt?: string | null;
  adminComment?: string | null;
  registeredVersion?: number | null;
  schemaRegistryId?: number | null;
}

export interface ApprovalDecisionRequest {
  comment?: string;
}
