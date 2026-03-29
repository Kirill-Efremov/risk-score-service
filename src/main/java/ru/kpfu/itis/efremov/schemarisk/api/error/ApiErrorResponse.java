package ru.kpfu.itis.efremov.schemarisk.api.error;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
@Schema(description = "Стандартный формат ошибки API")
public record ApiErrorResponse(
        @Schema(description = "Время возникновения ошибки")
        Instant timestamp,
        @Schema(description = "HTTP статус", example = "400")
        int status,
        @Schema(description = "Машиночитаемый код ошибки", example = "VALIDATION_ERROR")
        String errorCode,
        @Schema(description = "Сообщение об ошибке", example = "Request validation failed")
        String message,
        @Schema(description = "Путь запроса", example = "/api/v1/subjects/user-created/checks")
        String path,
        @Schema(description = "Детали ошибок валидации")
        List<ApiFieldError> details
) {
}





