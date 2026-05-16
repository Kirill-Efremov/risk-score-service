import type { SchemaAnalysisResponse } from "./analysis";

export type SchemaPromotionStatus =
  | "REGISTERED"
  | "BLOCKED_BY_GOVERNANCE"
  | "REQUIRES_MANUAL_APPROVAL"
  | "REQUIRES_CONSUMER_UPGRADE"
  | "SUGGEST_NEW_SUBJECT"
  | "REGISTRY_REJECTED"
  | "ANALYSIS_ONLY";

export interface SchemaPromotionResponse {
  subject: string;
  registered: boolean;
  oldVersion?: number | null;
  registeredVersion?: number | null;
  schemaRegistryId?: number | null;
  registrationStatus: SchemaPromotionStatus;
  registrationMessage: string;
  analysis?: SchemaAnalysisResponse | null;
  oldSchemaText?: string | null;
  newSchemaText?: string | null;
}
