import type { ImpactGraph } from "./graph";

export type RiskLevel = "LOW" | "MEDIUM" | "HIGH";
export type TechnicalDecision = "ALLOW" | "WARN" | "BLOCK";
export type GovernanceDecision =
  | "ALLOW"
  | "ALLOW_WITH_CAUTION"
  | "REQUIRE_CONSUMER_UPGRADE_FIRST"
  | "REJECT"
  | "SUGGEST_NEW_SUBJECT";

export interface RiskFactor {
  code: string;
  message: string;
  weight: number;
  source: "COMPATIBILITY" | "DIFF" | "IMPACT" | "GOVERNANCE";
}

export interface StructuredRecommendation {
  code: string;
  severity: "LOW" | "MEDIUM" | "HIGH";
  target: string;
  message: string;
  action: string;
}

export interface FieldChange {
  fieldName: string;
  oldType?: string | null;
  newType?: string | null;
  oldDefault?: string | null;
  newDefault?: string | null;
  type: string;
}

export interface DiffResult {
  schemaName?: string | null;
  changes: FieldChange[];
}

export interface ImpactResponse {
  affectedConsumersCount: number;
  affectedProducersCount: number;
  criticalServices: string[];
  breaking: boolean;
}

export interface SchemaAnalysisResponse {
  compatible: boolean;
  mode: string;
  issues: unknown[];
  diff?: DiffResult | null;
  riskScore: number;
  riskLevel: RiskLevel;
  decision: TechnicalDecision;
  governanceDecision?: GovernanceDecision | null;
  decisionExplanation: string[];
  riskFactors: RiskFactor[];
  recommendations: string[];
  structuredRecommendations: StructuredRecommendation[];
  impact?: ImpactResponse | null;
  impactGraph?: ImpactGraph | null;
  oldSchemaText?: string | null;
  newSchemaText?: string | null;
}

export interface AnalysisRecordResponse extends SchemaAnalysisResponse {
  id: number;
  subject: string;
  oldVersion?: number | null;
  newVersion?: number | null;
  compatibilityMode: string;
  formalCompatible: boolean;
  promotionAttempted?: boolean | null;
  registered?: boolean | null;
  registrationStatus?: string | null;
  registeredVersion?: number | null;
  schemaRegistryId?: number | null;
  createdAt: string;
  createdBy?: string | null;
}
