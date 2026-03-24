package ru.kpfu.itis.efremov.schemarisk.api.error;

import lombok.Builder;

@Builder
public record ApiFieldError(
        String field,
        Object rejectedValue,
        String message
) {
}




