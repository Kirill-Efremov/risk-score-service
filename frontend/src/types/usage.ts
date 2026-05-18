export interface ServiceResponse {
  id: number;
  name: string;
  critical: boolean;
  active: boolean;
  owner?: string | null;
  description?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface SchemaVersionResponse {
  subject: string;
  version: number;
  schemaText?: string | null;
  schemaType?: string | null;
  compatibilityMode?: string | null;
  status?: string | null;
  sourceType?: string | null;
  description?: string | null;
  externalSchemaId?: string | null;
  createdAt?: string | null;
}

export interface ServiceUsageResponse {
  id: number;
  serviceId: number;
  serviceName?: string | null;
  critical: boolean;
  serviceActive?: boolean;
  subject: string;
  version: number | null;
  role: "PRODUCER" | "CONSUMER";
  status?: string | null;
  active: boolean;
  createdAt?: string | null;
  updatedAt?: string | null;
  activeFrom?: string | null;
  activeTo?: string | null;
}

export interface CreateServiceRequest {
  name: string;
  critical: boolean;
  owner?: string;
  description?: string;
}

export interface UpdateServiceRequest {
  name?: string;
  critical?: boolean;
  active?: boolean;
  owner?: string;
  description?: string;
}

export interface CreateServiceUsageRequest {
  subject: string;
  version: number;
  role: "PRODUCER" | "CONSUMER";
  active?: boolean;
}

export interface UpdateServiceUsageRequest {
  subject?: string;
  version?: number;
  role?: "PRODUCER" | "CONSUMER";
  active?: boolean;
}

export interface MigrateServiceUsageRequest {
  targetVersion: number;
}
