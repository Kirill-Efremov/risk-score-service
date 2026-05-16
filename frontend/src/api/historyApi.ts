import { apiRequest } from "./client";
import type { AnalysisRecordResponse } from "../types/analysis";

export const historyApi = {
  getSubjectAnalysisHistory: (subject: string) =>
    apiRequest<AnalysisRecordResponse[]>(`/api/v1/subjects/${subject}/checks`),
  getAnalysisById: (id: string | number) =>
    apiRequest<AnalysisRecordResponse>(`/api/v1/checks/${id}`),
};
