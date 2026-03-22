package ru.kpfu.itis.efremov.schemarisk.api.dto;

import ru.kpfu.itis.efremov.schemarisk.application.usage.model.ServiceInfo;

import java.time.Instant;

public record ServiceResponse(
        Long id,
        String name,
        boolean critical,
        Instant createdAt
) {
    public static ServiceResponse fromInfo(ServiceInfo info) {
        return new ServiceResponse(info.id(), info.name(), info.critical(), info.createdAt());
    }
}
