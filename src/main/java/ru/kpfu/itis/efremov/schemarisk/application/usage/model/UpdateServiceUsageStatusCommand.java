package ru.kpfu.itis.efremov.schemarisk.application.usage.model;

import java.time.Instant;

public record UpdateServiceUsageStatusCommand(
        Long usageId,
        UsageStatus status,
        boolean active,
        Instant activeTo
) {
}
