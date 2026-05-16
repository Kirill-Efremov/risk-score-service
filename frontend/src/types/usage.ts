export interface ServiceResponse {
  id: number;
  name: string;
  critical: boolean;
  createdAt?: string | null;
}

export interface ServiceUsageResponse {
  id: number;
  serviceId: number;
  serviceName: string;
  critical: boolean;
  subject: string;
  version?: number | null;
  role: "PRODUCER" | "CONSUMER";
  status: string;
  active: boolean;
  createdAt?: string | null;
  activeFrom?: string | null;
  activeTo?: string | null;
}

export interface SchemaSubjectResponse {
  name: string;
  schemaType: string;
  defaultCompatibilityMode: string;
  description?: string | null;
}

export interface SchemaVersionResponse {
  subject: SchemaSubjectResponse;
  version: number;
  schemaText: string;
  schemaHash: string;
  status: string;
  sourceType: string;
  externalSchemaId?: string | null;
  createdAt?: string | null;
}
