import { ApiError } from "../api/client";

const ERROR_CODE_MESSAGES: Record<string, string> = {
  INVALID_CREDENTIALS: "Invalid username or password.",
  ACCESS_DENIED: "You do not have permission to perform this action.",
  AUTHENTICATION_REQUIRED: "Authentication is required.",
  INVALID_TOKEN: "Your session has expired or the token is invalid.",
  SELF_DEACTIVATION_NOT_ALLOWED: "You cannot deactivate your own account.",
  LAST_ADMIN_CANNOT_BE_DISABLED:
    "The last active administrator cannot be disabled.",
  USER_NOT_FOUND: "The requested user was not found.",
  USER_ALREADY_EXISTS: "A user with this username already exists.",
  APPROVAL_BASELINE_CHANGED:
    "The latest schema version changed after the approval request was created. Run the analysis again.",
  INVALID_APPROVAL_STATE:
    "This approval request has already been processed or is not available for this action.",
  SCHEMA_REGISTRY_CONFLICT:
    "Schema Registry rejected the schema publication.",
};

export function getErrorMessageByCode(errorCode?: string | null) {
  if (!errorCode) {
    return undefined;
  }

  return ERROR_CODE_MESSAGES[errorCode];
}

export function getUserFacingErrorMessage(error?: ApiError | null) {
  if (!error) {
    return undefined;
  }

  return getErrorMessageByCode(error.errorCode) ?? error.message;
}
