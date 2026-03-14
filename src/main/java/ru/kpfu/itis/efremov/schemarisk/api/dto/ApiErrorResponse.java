package ru.kpfu.itis.efremov.schemarisk.api.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String errorCode,
        String message,
        String path,
        List<ApiFieldError> details
) {
}
