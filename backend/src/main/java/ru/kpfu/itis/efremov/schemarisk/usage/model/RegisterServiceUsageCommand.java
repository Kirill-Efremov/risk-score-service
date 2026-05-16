package ru.kpfu.itis.efremov.schemarisk.usage.model;

public record RegisterServiceUsageCommand(
        Long serviceId,
        String subject,
        Integer version,
        ServiceRole role,
        Boolean active
) {
}




