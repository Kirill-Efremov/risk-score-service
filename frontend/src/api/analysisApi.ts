import { apiRequest } from "./client";
import type { SchemaAnalysisResponse } from "../types/analysis";

export interface RawAnalysisRequest {
  schemaType: string;
  compatibilityMode: string;
  oldSchema: string;
  newSchema: string;
}

export interface VersionedAnalysisRequest {
  oldVersion?: number;
  newVersion?: number;
  newSchema?: string;
  schemaType?: string;
  compatibilityMode?: string;
}

export const analysisApi = {
  runRawAnalysis: (request: RawAnalysisRequest) =>
    apiRequest<SchemaAnalysisResponse>("/api/v1/checks", {
      method: "POST",
      body: JSON.stringify(request),
    }),
  runVersionedAnalysis: (subject: string, request: VersionedAnalysisRequest) =>
    apiRequest<SchemaAnalysisResponse>(`/api/v1/subjects/${subject}/checks`, {
      method: "POST",
      body: JSON.stringify(request),
    }),
};
