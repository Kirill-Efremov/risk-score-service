import { apiRequest } from "./client";
import type { SchemaVersionResponse } from "../types/usage";

export interface RegisterSchemaVersionRequest {
  schemaType: string;
  defaultCompatibilityMode?: string;
  description?: string;
  schemaText: string;
  status?: string;
  sourceType?: string;
  externalSchemaId?: string;
}

export const catalogApi = {
  getSubjects: () => apiRequest<string[]>("/api/v1/subjects"),
  getVersions: (subject: string) =>
    apiRequest<SchemaVersionResponse[]>(`/api/v1/subjects/${subject}/versions`),
  getVersion: (subject: string, version: number) =>
    apiRequest<SchemaVersionResponse>(
      `/api/v1/subjects/${subject}/versions/${version}`,
    ),
  getLatestVersion: (subject: string) =>
    apiRequest<SchemaVersionResponse>(
      `/api/v1/subjects/${subject}/versions/latest`,
    ),
  registerSchemaVersion: (subject: string, request: RegisterSchemaVersionRequest) =>
    apiRequest<SchemaVersionResponse>(`/api/v1/subjects/${subject}/versions`, {
      method: "POST",
      body: JSON.stringify(request),
    }),
};
