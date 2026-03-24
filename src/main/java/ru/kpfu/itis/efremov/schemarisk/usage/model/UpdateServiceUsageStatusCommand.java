package ru.kpfu.itis.efremov.schemarisk.usage.model;

import java.time.Instant;

public record UpdateServiceUsageStatusCommand(
        Long usageId,
        UsageStatus status,
        boolean active,
        Instant activeTo
) {
}




