package ru.kpfu.itis.efremov.schemarisk.api.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.kpfu.itis.efremov.schemarisk.approval.exception.ApprovalBaselineChangedException;
import ru.kpfu.itis.efremov.schemarisk.approval.exception.ApprovalNotAllowedException;
import ru.kpfu.itis.efremov.schemarisk.approval.exception.InvalidApprovalStateException;
import ru.kpfu.itis.efremov.schemarisk.approval.exception.SchemaApprovalNotFoundException;
import ru.kpfu.itis.efremov.schemarisk.auth.exception.AuthenticationRequiredException;
import ru.kpfu.itis.efremov.schemarisk.auth.exception.InvalidCredentialsException;
import ru.kpfu.itis.efremov.schemarisk.auth.exception.LastAdminCannotBeDisabledException;
import ru.kpfu.itis.efremov.schemarisk.auth.exception.SelfDeactivationNotAllowedException;
import ru.kpfu.itis.efremov.schemarisk.auth.exception.UserAlreadyExistsException;
import ru.kpfu.itis.efremov.schemarisk.auth.exception.UserDisabledException;
import ru.kpfu.itis.efremov.schemarisk.auth.exception.UserNotFoundException;
import ru.kpfu.itis.efremov.schemarisk.catalog.exception.SchemaRegistryConflictException;
import ru.kpfu.itis.efremov.schemarisk.common.exception.InvalidRequestException;
import ru.kpfu.itis.efremov.schemarisk.common.exception.InvalidSchemaException;
import ru.kpfu.itis.efremov.schemarisk.common.exception.InvalidUsageOperationException;
import ru.kpfu.itis.efremov.schemarisk.common.exception.ResourceNotFoundException;
import ru.kpfu.itis.efremov.schemarisk.common.exception.SchemaRegistryUnavailableException;
import ru.kpfu.itis.efremov.schemarisk.common.exception.SchemaSubjectNotFoundException;
import ru.kpfu.itis.efremov.schemarisk.common.exception.SchemaVersionNotFoundException;
import ru.kpfu.itis.efremov.schemarisk.common.exception.ServiceAlreadyExistsException;
import ru.kpfu.itis.efremov.schemarisk.common.exception.ServiceNotFoundException;
import ru.kpfu.itis.efremov.schemarisk.common.exception.ServiceUsageAlreadyExistsException;
import ru.kpfu.itis.efremov.schemarisk.common.exception.ServiceUsageNotFoundException;
import ru.kpfu.itis.efremov.schemarisk.common.exception.UnsupportedSchemaTypeException;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiErrorResponse handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        List<ApiFieldError> details = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toFieldError)
                .toList();

        return buildError(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "Request validation failed",
                request,
                details
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({ConstraintViolationException.class, MethodArgumentTypeMismatchException.class})
    public ApiErrorResponse handleConstraintViolation(
            Exception exception,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                exception.getMessage(),
                request,
                List.of()
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiErrorResponse handleUnreadableMessage(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        if (exception.getMessage() != null && exception.getMessage().contains("Unsupported schema type")) {
            return buildError(
                    HttpStatus.BAD_REQUEST,
                    "UNSUPPORTED_SCHEMA_TYPE",
                    "Unsupported schema type",
                    request,
                    List.of()
            );
        }
        return buildError(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "Request body is invalid or contains unsupported enum values",
                request,
                List.of()
        );
    }

    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ApiErrorResponse handleMethodNotSupported(
            HttpRequestMethodNotSupportedException exception,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.METHOD_NOT_ALLOWED,
                "METHOD_NOT_ALLOWED",
                exception.getMessage(),
                request,
                List.of()
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidSchemaException.class)
    public ApiErrorResponse handleInvalidSchema(
            InvalidSchemaException exception,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.BAD_REQUEST,
                "INVALID_SCHEMA",
                exception.getMessage(),
                request,
                List.of()
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(UnsupportedSchemaTypeException.class)
    public ApiErrorResponse handleUnsupportedSchemaType(
            UnsupportedSchemaTypeException exception,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.BAD_REQUEST,
                "UNSUPPORTED_SCHEMA_TYPE",
                exception.getMessage(),
                request,
                List.of()
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidUsageOperationException.class)
    public ApiErrorResponse handleInvalidUsageOperation(
            InvalidUsageOperationException exception,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.BAD_REQUEST,
                "INVALID_USAGE_OPERATION",
                exception.getMessage(),
                request,
                List.of()
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({InvalidRequestException.class, IllegalArgumentException.class})
    public ApiErrorResponse handleInvalidRequest(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.BAD_REQUEST,
                "INVALID_REQUEST",
                exception.getMessage(),
                request,
                List.of()
        );
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ServiceNotFoundException.class)
    public ApiErrorResponse handleServiceNotFound(
            ServiceNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.NOT_FOUND,
                "SERVICE_NOT_FOUND",
                exception.getMessage(),
                request,
                List.of()
        );
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ServiceAlreadyExistsException.class)
    public ApiErrorResponse handleServiceAlreadyExists(
            ServiceAlreadyExistsException exception,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.CONFLICT,
                "SERVICE_ALREADY_EXISTS",
                exception.getMessage(),
                request,
                List.of()
        );
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ServiceUsageNotFoundException.class)
    public ApiErrorResponse handleServiceUsageNotFound(
            ServiceUsageNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.NOT_FOUND,
                "SERVICE_USAGE_NOT_FOUND",
                exception.getMessage(),
                request,
                List.of()
        );
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ServiceUsageAlreadyExistsException.class)
    public ApiErrorResponse handleServiceUsageAlreadyExists(
            ServiceUsageAlreadyExistsException exception,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.CONFLICT,
                "SERVICE_USAGE_ALREADY_EXISTS",
                exception.getMessage(),
                request,
                List.of()
        );
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(SchemaSubjectNotFoundException.class)
    public ApiErrorResponse handleSchemaSubjectNotFound(
            SchemaSubjectNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.NOT_FOUND,
                "SCHEMA_SUBJECT_NOT_FOUND",
                exception.getMessage(),
                request,
                List.of()
        );
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(SchemaVersionNotFoundException.class)
    public ApiErrorResponse handleSchemaVersionNotFound(
            SchemaVersionNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.NOT_FOUND,
                "SCHEMA_VERSION_NOT_FOUND",
                exception.getMessage(),
                request,
                List.of()
        );
    }

    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    @ExceptionHandler(SchemaRegistryUnavailableException.class)
    public ApiErrorResponse handleSchemaRegistryUnavailable(
            SchemaRegistryUnavailableException exception,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.SERVICE_UNAVAILABLE,
                "SCHEMA_REGISTRY_UNAVAILABLE",
                exception.getMessage(),
                request,
                List.of()
        );
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ApiErrorResponse handleNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.NOT_FOUND,
                "NOT_FOUND",
                exception.getMessage(),
                request,
                List.of()
        );
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(SchemaApprovalNotFoundException.class)
    public ApiErrorResponse handleSchemaApprovalNotFound(
            SchemaApprovalNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.NOT_FOUND,
                "SCHEMA_APPROVAL_NOT_FOUND",
                exception.getMessage(),
                request,
                List.of()
        );
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(SchemaRegistryConflictException.class)
    public ApiErrorResponse handleSchemaRegistryConflict(
            SchemaRegistryConflictException exception,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.CONFLICT,
                "SCHEMA_REGISTRY_CONFLICT",
                "Schema Registry rejected schema registration: " + exception.getMessage(),
                request,
                List.of()
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidApprovalStateException.class)
    public ApiErrorResponse handleInvalidApprovalState(
            InvalidApprovalStateException exception,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.BAD_REQUEST,
                "INVALID_APPROVAL_STATE",
                exception.getMessage(),
                request,
                List.of()
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ApprovalNotAllowedException.class)
    public ApiErrorResponse handleApprovalNotAllowed(
            ApprovalNotAllowedException exception,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.BAD_REQUEST,
                "APPROVAL_NOT_ALLOWED",
                exception.getMessage(),
                request,
                List.of()
        );
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ApprovalBaselineChangedException.class)
    public ApiErrorResponse handleApprovalBaselineChanged(
            ApprovalBaselineChangedException exception,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.CONFLICT,
                "APPROVAL_BASELINE_CHANGED",
                exception.getMessage(),
                request,
                List.of()
        );
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ApiErrorResponse handleUserAlreadyExists(
            UserAlreadyExistsException exception,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.CONFLICT,
                "USER_ALREADY_EXISTS",
                exception.getMessage(),
                request,
                List.of()
        );
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(UserNotFoundException.class)
    public ApiErrorResponse handleUserNotFound(
            UserNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.NOT_FOUND,
                "USER_NOT_FOUND",
                exception.getMessage(),
                request,
                List.of()
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(SelfDeactivationNotAllowedException.class)
    public ApiErrorResponse handleSelfDeactivationNotAllowed(
            SelfDeactivationNotAllowedException exception,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.BAD_REQUEST,
                "SELF_DEACTIVATION_NOT_ALLOWED",
                exception.getMessage(),
                request,
                List.of()
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(LastAdminCannotBeDisabledException.class)
    public ApiErrorResponse handleLastAdminCannotBeDisabled(
            LastAdminCannotBeDisabledException exception,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.BAD_REQUEST,
                "LAST_ADMIN_CANNOT_BE_DISABLED",
                exception.getMessage(),
                request,
                List.of()
        );
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler({InvalidCredentialsException.class, UserDisabledException.class, AuthenticationRequiredException.class})
    public ApiErrorResponse handleAuthenticationFailure(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        String errorCode = "AUTHENTICATION_REQUIRED";
        if (exception instanceof InvalidCredentialsException) {
            errorCode = "INVALID_CREDENTIALS";
        } else if (exception instanceof UserDisabledException) {
            errorCode = "USER_DISABLED";
        }

        return buildError(
                HttpStatus.UNAUTHORIZED,
                errorCode,
                exception.getMessage(),
                request,
                List.of()
        );
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public ApiErrorResponse handleAccessDenied(
            AccessDeniedException exception,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.FORBIDDEN,
                "ACCESS_DENIED",
                "Access is denied",
                request,
                List.of()
        );
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ApiErrorResponse handleUnexpected(
            Exception exception,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                "Unexpected server error",
                request,
                List.of()
        );
    }

    private ApiFieldError toFieldError(FieldError error) {
        return ApiFieldError.builder()
                .field(error.getField())
                .rejectedValue(error.getRejectedValue())
                .message(error.getDefaultMessage())
                .build();
    }

    private ApiErrorResponse buildError(
            HttpStatus status,
            String errorCode,
            String message,
            HttpServletRequest request,
            List<ApiFieldError> details
    ) {
        return ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .errorCode(errorCode)
                .message(message)
                .path(request.getRequestURI())
                .details(details)
                .build();
    }
}
