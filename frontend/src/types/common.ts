export interface ApiFieldError {
  field: string;
  rejectedValue?: unknown;
  message: string;
}

export interface ApiErrorResponse {
  timestamp: string;
  status: number;
  errorCode: string;
  message: string;
  path: string;
  details: ApiFieldError[];
}

export interface SystemStatusResponse {
  backend: string;
  schemaRegistry: string;
  database: string;
}
