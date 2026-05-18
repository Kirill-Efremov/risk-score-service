package ru.kpfu.itis.efremov.schemarisk.usage.audit.model;

import ru.kpfu.itis.efremov.schemarisk.usage.model.ServiceRole;

import java.time.Instant;

public record ServiceUsageAuditCommand(
        Long serviceId,
        String serviceName,
        Long usageId,
        ServiceUsageAuditAction action,
        String oldSubject,
        String newSubject,
        Integer oldVersion,
        Integer newVersion,
        ServiceRole oldRole,
        ServiceRole newRole,
        Boolean oldActive,
        Boolean newActive,
        Boolean oldServiceActive,
        Boolean newServiceActive,
        String changedBy,
        String reason,
        Instant createdAt
) {
}
