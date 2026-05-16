import { apiRequest } from "./client";
import type { SchemaPromotionResponse } from "../types/promotion";

export interface PromotionRequest {
  schemaType: string;
  compatibilityMode?: string;
  description?: string;
  createdBy?: string;
  schemaText: string;
}

export const promotionApi = {
  promoteSchema: (subject: string, request: PromotionRequest) =>
    apiRequest<SchemaPromotionResponse>(`/api/v1/subjects/${subject}/promotions`, {
      method: "POST",
      body: JSON.stringify(request),
    }),
};
