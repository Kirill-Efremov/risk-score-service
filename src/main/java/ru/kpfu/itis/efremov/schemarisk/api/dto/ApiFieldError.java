package ru.kpfu.itis.efremov.schemarisk.api.dto;

import lombok.Builder;

@Builder
public record ApiFieldError(
        String field,
        Object rejectedValue,
        String message
) {
}
