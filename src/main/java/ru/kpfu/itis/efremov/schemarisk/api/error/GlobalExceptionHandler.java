package ru.kpfu.itis.efremov.schemarisk.api.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.kpfu.itis.efremov.schemarisk.common.exception.InvalidRequestException;
import ru.kpfu.itis.efremov.schemarisk.common.exception.InvalidSchemaException;
import ru.kpfu.itis.efremov.schemarisk.common.exception.ResourceNotFoundException;
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
                "INVALID_REQUEST",
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
        return buildError(
                HttpStatus.BAD_REQUEST,
                "INVALID_REQUEST_BODY",
                "Request body is invalid or contains unsupported enum values",
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




