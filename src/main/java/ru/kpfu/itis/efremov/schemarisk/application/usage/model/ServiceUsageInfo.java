package ru.kpfu.itis.efremov.schemarisk.application.usage.model;

import java.time.Instant;

public record ServiceUsageInfo(
        Long id,
        Long serviceId,
        String serviceName,
        boolean critical,
        String subject,
        Integer version,
        ServiceRole role,
        boolean active,
        Instant createdAt
) {
}
