package ru.kpfu.itis.efremov.schemarisk.usage.model;

import java.time.Instant;

public record ServiceInfo(
        Long id,
        String name,
        boolean critical,
        boolean active,
        String owner,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
}




