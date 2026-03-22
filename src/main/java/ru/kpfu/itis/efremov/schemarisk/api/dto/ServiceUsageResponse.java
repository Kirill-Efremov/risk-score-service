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
        boolean active,
        Instant createdAt
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
                info.active(),
                info.createdAt()
        );
    }
}
