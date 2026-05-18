package ru.kpfu.itis.efremov.schemarisk.usage.audit.service;

import org.springframework.stereotype.Component;
import ru.kpfu.itis.efremov.schemarisk.usage.audit.model.ServiceUsageAuditCommand;
import ru.kpfu.itis.efremov.schemarisk.usage.audit.model.ServiceUsageAuditRecord;
import ru.kpfu.itis.efremov.schemarisk.usage.audit.persistence.entity.ServiceUsageAuditEntity;

@Component
public class ServiceUsageAuditMapper {

    public ServiceUsageAuditEntity toEntity(ServiceUsageAuditCommand command) {
        ServiceUsageAuditEntity entity = new ServiceUsageAuditEntity();
        entity.setServiceId(command.serviceId());
        entity.setServiceName(command.serviceName());
        entity.setUsageId(command.usageId());
        entity.setAction(command.action());
        entity.setOldSubject(command.oldSubject());
        entity.setNewSubject(command.newSubject());
        entity.setOldVersion(command.oldVersion());
        entity.setNewVersion(command.newVersion());
        entity.setOldRole(command.oldRole());
        entity.setNewRole(command.newRole());
        entity.setOldActive(command.oldActive());
        entity.setNewActive(command.newActive());
        entity.setOldServiceActive(command.oldServiceActive());
        entity.setNewServiceActive(command.newServiceActive());
        entity.setChangedBy(command.changedBy());
        entity.setReason(command.reason());
        entity.setCreatedAt(command.createdAt());
        return entity;
    }

    public ServiceUsageAuditRecord toRecord(ServiceUsageAuditEntity entity) {
        return new ServiceUsageAuditRecord(
                entity.getId(),
                entity.getServiceId(),
                entity.getServiceName(),
                entity.getUsageId(),
                entity.getAction(),
                entity.getOldSubject(),
                entity.getNewSubject(),
                entity.getOldVersion(),
                entity.getNewVersion(),
                entity.getOldRole(),
                entity.getNewRole(),
                entity.getOldActive(),
                entity.getNewActive(),
                entity.getOldServiceActive(),
                entity.getNewServiceActive(),
                entity.getChangedBy(),
                entity.getReason(),
                entity.getCreatedAt()
        );
    }
}
