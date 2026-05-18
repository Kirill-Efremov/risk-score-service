package ru.kpfu.itis.efremov.schemarisk.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.kpfu.itis.efremov.schemarisk.usage.audit.model.ServiceUsageAuditRecord;

import java.time.Instant;

@Schema(description = "History of service usage map changes")
public record ServiceUsageAuditResponse(
        Long id,
        Long serviceId,
        String serviceName,
        Long usageId,
        String action,
        String oldSubject,
        String newSubject,
        Integer oldVersion,
        Integer newVersion,
        String oldRole,
        String newRole,
        Boolean oldActive,
        Boolean newActive,
        String changedBy,
        Instant createdAt
) {
    public static ServiceUsageAuditResponse fromRecord(ServiceUsageAuditRecord record) {
        return new ServiceUsageAuditResponse(
                record.id(),
                record.serviceId(),
                record.serviceName(),
                record.usageId(),
                record.action().name(),
                record.oldSubject(),
                record.newSubject(),
                record.oldVersion(),
                record.newVersion(),
                record.oldRole() != null ? record.oldRole().name() : null,
                record.newRole() != null ? record.newRole().name() : null,
                record.oldActive(),
                record.newActive(),
                record.changedBy(),
                record.createdAt()
        );
    }
}
