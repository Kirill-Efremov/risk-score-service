package ru.kpfu.itis.efremov.schemarisk.usage.model;

import java.time.Instant;

public record ServiceUsageInfo(
        Long id,
        Long serviceId,
        String serviceName,
        boolean critical,
        boolean serviceActive,
        String subject,
        Integer version,
        ServiceRole role,
        UsageStatus status,
        boolean active,
        Instant createdAt,
        Instant updatedAt,
        Instant activeFrom,
        Instant activeTo
) {
}




