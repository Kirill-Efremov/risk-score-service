package ru.kpfu.itis.efremov.schemarisk.usage.audit.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kpfu.itis.efremov.schemarisk.usage.audit.model.ServiceUsageAuditAction;
import ru.kpfu.itis.efremov.schemarisk.usage.audit.model.ServiceUsageAuditCommand;
import ru.kpfu.itis.efremov.schemarisk.usage.audit.model.ServiceUsageAuditRecord;
import ru.kpfu.itis.efremov.schemarisk.usage.audit.persistence.repository.ServiceUsageAuditRepository;

import java.util.List;

@Service
public class ServiceUsageAuditService {

    private static final int MAX_LIMIT = 200;

    private final ServiceUsageAuditRepository repository;
    private final ServiceUsageAuditMapper mapper;

    public ServiceUsageAuditService(ServiceUsageAuditRepository repository, ServiceUsageAuditMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    public ServiceUsageAuditRecord record(ServiceUsageAuditCommand command) {
        ServiceUsageAuditCommand normalized = new ServiceUsageAuditCommand(
                command.serviceId(),
                command.serviceName(),
                command.usageId(),
                command.action(),
                command.oldSubject(),
                command.newSubject(),
                command.oldVersion(),
                command.newVersion(),
                command.oldRole(),
                command.newRole(),
                command.oldActive(),
                command.newActive(),
                command.oldServiceActive(),
                command.newServiceActive(),
                normalizeChangedBy(command.changedBy()),
                command.reason(),
                command.createdAt()
        );
        return mapper.toRecord(repository.save(mapper.toEntity(normalized)));
    }

    @Transactional(readOnly = true)
    public List<ServiceUsageAuditRecord> getServiceAudit(
            Long serviceId,
            ServiceUsageAuditAction action,
            Integer limit
    ) {
        int resolvedLimit = resolveLimit(limit, 50);
        return (action == null
                        ? repository.findServiceAudit(serviceId, PageRequest.of(0, resolvedLimit))
                        : repository.findServiceAuditByAction(
                                serviceId,
                                action,
                                PageRequest.of(0, resolvedLimit)
                        )).stream()
                .map(mapper::toRecord)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ServiceUsageAuditRecord> getUsageAudit(Long serviceId, Long usageId, Integer limit) {
        return repository.findUsageAudit(serviceId, usageId, PageRequest.of(0, resolveLimit(limit, 50))).stream()
                .map(mapper::toRecord)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ServiceUsageAuditRecord> searchAudit(
            Long serviceId,
            Long usageId,
            ServiceUsageAuditAction action,
            Integer limit
    ) {
        int resolvedLimit = resolveLimit(limit, 50);
        return selectAuditRecords(serviceId, usageId, action, resolvedLimit).stream()
                .map(mapper::toRecord)
                .toList();
    }

    private List<ru.kpfu.itis.efremov.schemarisk.usage.audit.persistence.entity.ServiceUsageAuditEntity> selectAuditRecords(
            Long serviceId,
            Long usageId,
            ServiceUsageAuditAction action,
            int resolvedLimit
    ) {
        var pageable = PageRequest.of(0, resolvedLimit);

        if (serviceId != null && usageId != null && action != null) {
            return repository.findAllAuditByServiceIdAndUsageIdAndAction(serviceId, usageId, action, pageable);
        }
        if (serviceId != null && usageId != null) {
            return repository.findAllAuditByServiceIdAndUsageId(serviceId, usageId, pageable);
        }
        if (serviceId != null && action != null) {
            return repository.findAllAuditByServiceIdAndAction(serviceId, action, pageable);
        }
        if (usageId != null && action != null) {
            return repository.findAllAuditByUsageIdAndAction(usageId, action, pageable);
        }
        if (serviceId != null) {
            return repository.findAllAuditByServiceId(serviceId, pageable);
        }
        if (usageId != null) {
            return repository.findAllAuditByUsageId(usageId, pageable);
        }
        if (action != null) {
            return repository.findAllAuditByAction(action, pageable);
        }
        return repository.findAllAudit(pageable);
    }

    private int resolveLimit(Integer requestedLimit, int defaultValue) {
        if (requestedLimit == null) {
            return defaultValue;
        }
        return Math.max(1, Math.min(requestedLimit, MAX_LIMIT));
    }

    private String normalizeChangedBy(String value) {
        if (value == null || value.isBlank()) {
            return "system";
        }
        return value.trim();
    }
}
