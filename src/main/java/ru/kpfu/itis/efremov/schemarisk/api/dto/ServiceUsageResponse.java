package ru.kpfu.itis.efremov.schemarisk.api.dto;

import ru.kpfu.itis.efremov.schemarisk.application.usage.model.ServiceUsageInfo;

import java.time.Instant;

public record ServiceUsageResponse(
        Long id,
        Long serviceId,
        String serviceName,
        boolean critical,
        String subject,
        Integer version,
        String role,
        String status,
        boolean active,
        Instant createdAt,
        Instant activeFrom,
        Instant activeTo
) {
    public static ServiceUsageResponse fromInfo(ServiceUsageInfo info) {
        return new ServiceUsageResponse(
                info.id(),
                info.serviceId(),
                info.serviceName(),
                info.critical(),
                info.subject(),
                info.version(),
                info.role().name(),
                info.status().name(),
                info.active(),
                info.createdAt(),
                info.activeFrom(),
                info.activeTo()
        );
    }
}
