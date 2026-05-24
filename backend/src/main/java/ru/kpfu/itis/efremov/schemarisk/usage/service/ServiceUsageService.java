package ru.kpfu.itis.efremov.schemarisk.usage.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kpfu.itis.efremov.schemarisk.auth.service.CurrentUserService;
import ru.kpfu.itis.efremov.schemarisk.common.exception.InvalidUsageOperationException;
import ru.kpfu.itis.efremov.schemarisk.common.exception.ResourceNotFoundException;
import ru.kpfu.itis.efremov.schemarisk.common.exception.SchemaSubjectNotFoundException;
import ru.kpfu.itis.efremov.schemarisk.common.exception.SchemaVersionNotFoundException;
import ru.kpfu.itis.efremov.schemarisk.common.exception.ServiceAlreadyExistsException;
import ru.kpfu.itis.efremov.schemarisk.common.exception.ServiceUsageAlreadyExistsException;
import ru.kpfu.itis.efremov.schemarisk.common.port.SchemaCatalog;
import ru.kpfu.itis.efremov.schemarisk.common.port.ServiceUsageRepository;
import ru.kpfu.itis.efremov.schemarisk.usage.audit.model.ServiceUsageAuditAction;
import ru.kpfu.itis.efremov.schemarisk.usage.audit.model.ServiceUsageAuditCommand;
import ru.kpfu.itis.efremov.schemarisk.usage.audit.model.ServiceUsageAuditRecord;
import ru.kpfu.itis.efremov.schemarisk.usage.audit.service.ServiceUsageAuditService;
import ru.kpfu.itis.efremov.schemarisk.usage.model.RegisterServiceCommand;
import ru.kpfu.itis.efremov.schemarisk.usage.model.RegisterServiceUsageCommand;
import ru.kpfu.itis.efremov.schemarisk.usage.model.ServiceInfo;
import ru.kpfu.itis.efremov.schemarisk.usage.model.ServiceRole;
import ru.kpfu.itis.efremov.schemarisk.usage.model.ServiceUsageInfo;
import ru.kpfu.itis.efremov.schemarisk.usage.model.UpdateServiceUsageStatusCommand;
import ru.kpfu.itis.efremov.schemarisk.usage.model.UsageStatus;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
public class ServiceUsageService {

    private final ServiceUsageRepository serviceUsageRepository;
    private final SchemaCatalog schemaCatalog;
    private final ServiceUsageAuditService auditService;
    private final CurrentUserService currentUserService;

    public ServiceUsageService(
            ServiceUsageRepository serviceUsageRepository,
            SchemaCatalog schemaCatalog,
            ServiceUsageAuditService auditService,
            CurrentUserService currentUserService
    ) {
        this.serviceUsageRepository = serviceUsageRepository;
        this.schemaCatalog = schemaCatalog;
        this.auditService = auditService;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public ServiceInfo registerService(RegisterServiceCommand command, String createdBy) {
        String resolvedChangedBy = resolveActor(createdBy);
        ServiceInfo created = serviceUsageRepository.registerService(command);
        auditService.record(auditCommand(
                created.id(),
                created.name(),
                null,
                ServiceUsageAuditAction.SERVICE_CREATED,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                created.active(),
                resolvedChangedBy
        ));
        return created;
    }

    public List<ServiceInfo> listServices(Boolean active, Boolean critical) {
        return serviceUsageRepository.listServices(active, critical);
    }

    public ServiceInfo getService(Long serviceId) {
        return serviceUsageRepository.getServiceById(serviceId);
    }

    @Transactional
    public ServiceInfo updateService(
            Long serviceId,
            String name,
            Boolean critical,
            Boolean active,
            String owner,
            String description,
            String changedBy
    ) {
        String resolvedChangedBy = resolveActor(changedBy);
        ServiceInfo current = serviceUsageRepository.getServiceById(serviceId);
        String normalizedName = normalizeName(name);
        String normalizedOwner = normalizeOptionalText(owner);
        String normalizedDescription = normalizeOptionalText(description);

        if (normalizedName != null && !normalizedName.equals(current.name())) {
            serviceUsageRepository.listServices(null, null).stream()
                    .filter(existing -> existing.name().equals(normalizedName) && !existing.id().equals(serviceId))
                    .findFirst()
                    .ifPresent(existing -> {
                        throw new ServiceAlreadyExistsException(normalizedName);
                    });
        }

        Boolean nextCritical = critical != null ? critical : current.critical();
        Boolean nextActive = active != null ? active : current.active();
        String nextName = normalizedName != null ? normalizedName : current.name();
        String nextOwner = owner != null ? normalizedOwner : current.owner();
        String nextDescription = description != null ? normalizedDescription : current.description();

        if (Objects.equals(nextName, current.name())
                && Objects.equals(nextCritical, current.critical())
                && Objects.equals(nextActive, current.active())
                && Objects.equals(nextOwner, current.owner())
                && Objects.equals(nextDescription, current.description())) {
            return current;
        }

        List<ServiceUsageInfo> affectedUsages = Boolean.TRUE.equals(current.active()) && Boolean.FALSE.equals(nextActive)
                ? serviceUsageRepository.getServiceUsages(serviceId, true, null, null)
                : List.of();

        ServiceInfo updated = serviceUsageRepository.updateService(
                serviceId,
                normalizedName,
                critical,
                active,
                owner,
                description,
                Instant.now()
        );

        ServiceUsageAuditAction action = Boolean.TRUE.equals(current.active()) && Boolean.FALSE.equals(updated.active())
                ? ServiceUsageAuditAction.SERVICE_DEACTIVATED
                : ServiceUsageAuditAction.SERVICE_UPDATED;

        auditService.record(auditCommand(
                updated.id(),
                updated.name(),
                null,
                action,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                current.active(),
                updated.active(),
                resolvedChangedBy
        ));

        if (action == ServiceUsageAuditAction.SERVICE_DEACTIVATED) {
            for (ServiceUsageInfo usage : affectedUsages) {
                auditService.record(usageAuditDeactivated(usage, resolvedChangedBy, false));
            }
        }

        return updated;
    }

    @Transactional
    public void deactivateService(Long serviceId) {
        String resolvedChangedBy = resolveActor(null);
        ServiceInfo current = serviceUsageRepository.getServiceById(serviceId);
        if (!current.active()) {
            return;
        }

        List<ServiceUsageInfo> affectedUsages = serviceUsageRepository.getServiceUsages(serviceId, true, null, null);
        ServiceInfo updated = serviceUsageRepository.deactivateService(serviceId, Instant.now());

        auditService.record(auditCommand(
                updated.id(),
                updated.name(),
                null,
                ServiceUsageAuditAction.SERVICE_DEACTIVATED,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                current.active(),
                updated.active(),
                resolvedChangedBy
        ));

        for (ServiceUsageInfo usage : affectedUsages) {
            auditService.record(usageAuditDeactivated(usage, resolvedChangedBy, false));
        }
    }

    @Transactional
    public ServiceUsageInfo registerUsage(RegisterServiceUsageCommand command, String createdBy) {
        String resolvedChangedBy = resolveActor(createdBy);
        ServiceInfo service = serviceUsageRepository.getServiceById(command.serviceId());
        if (!service.active()) {
            throw new InvalidUsageOperationException("Cannot create usage for inactive service: " + command.serviceId());
        }

        validateSchema(command.subject(), command.version());
        boolean active = command.active() == null || command.active();
        ensureNoDuplicateUsage(command.serviceId(), command.subject(), command.version(), command.role(), null, active);

        ServiceUsageInfo created = serviceUsageRepository.registerUsage(
                new RegisterServiceUsageCommand(
                        command.serviceId(),
                        normalizeSubject(command.subject()),
                        command.version(),
                        command.role(),
                        command.active()
                )
        );

        auditService.record(auditCommand(
                created.serviceId(),
                created.serviceName(),
                created.id(),
                ServiceUsageAuditAction.USAGE_CREATED,
                null,
                created.subject(),
                null,
                created.version(),
                null,
                created.role(),
                null,
                created.active(),
                created.serviceActive(),
                created.serviceActive(),
                resolvedChangedBy
        ));
        return created;
    }

    @Transactional
    public ServiceUsageInfo updateUsage(
            Long serviceId,
            Long usageId,
            String subject,
            Integer version,
            ServiceRole role,
            Boolean active,
            String changedBy
    ) {
        String resolvedChangedBy = resolveActor(changedBy);
        ServiceInfo service = serviceUsageRepository.getServiceById(serviceId);
        ServiceUsageInfo current = serviceUsageRepository.getUsageById(usageId);
        if (!current.serviceId().equals(serviceId)) {
            throw new InvalidUsageOperationException("Usage does not belong to service: " + usageId);
        }

        String nextSubject = subject != null ? normalizeSubject(subject) : current.subject();
        Integer nextVersion = version != null ? version : current.version();
        ServiceRole nextRole = role != null ? role : current.role();
        boolean nextActive = active != null ? active : current.active();

        if ((subject != null || version != null) || (!current.active() && nextActive)) {
            validateSchema(nextSubject, nextVersion);
        }

        ensureNoDuplicateUsage(serviceId, nextSubject, nextVersion, nextRole, usageId, nextActive);

        if (Objects.equals(nextSubject, current.subject())
                && Objects.equals(nextVersion, current.version())
                && Objects.equals(nextRole, current.role())
                && nextActive == current.active()) {
            return current;
        }

        if (nextActive && !service.active()) {
            throw new InvalidUsageOperationException("Cannot activate usage for inactive service: " + serviceId);
        }

        ServiceUsageInfo updated = serviceUsageRepository.updateUsage(
                serviceId,
                usageId,
                subject != null ? nextSubject : null,
                version,
                role,
                active,
                Instant.now()
        );

        auditService.record(auditCommand(
                updated.serviceId(),
                updated.serviceName(),
                updated.id(),
                ServiceUsageAuditAction.USAGE_UPDATED,
                current.subject(),
                updated.subject(),
                current.version(),
                updated.version(),
                current.role(),
                updated.role(),
                current.active(),
                updated.active(),
                current.serviceActive(),
                updated.serviceActive(),
                resolvedChangedBy
        ));
        return updated;
    }

    @Transactional
    public void deactivateUsage(Long serviceId, Long usageId) {
        String resolvedChangedBy = resolveActor(null);
        ServiceUsageInfo current = serviceUsageRepository.getUsageById(usageId);
        if (!current.serviceId().equals(serviceId)) {
            throw new InvalidUsageOperationException("Usage does not belong to service: " + usageId);
        }
        if (!current.active()) {
            return;
        }

        serviceUsageRepository.deactivateUsage(serviceId, usageId, Instant.now());
        auditService.record(usageAuditDeactivated(current, resolvedChangedBy, current.serviceActive()));
    }

    @Transactional
    public ServiceUsageInfo migrateUsage(Long serviceId, Long usageId, Integer targetVersion, String changedBy) {
        String resolvedChangedBy = resolveActor(changedBy);
        ServiceInfo service = serviceUsageRepository.getServiceById(serviceId);
        if (!service.active()) {
            throw new InvalidUsageOperationException("Cannot migrate usage for inactive service: " + serviceId);
        }

        ServiceUsageInfo current = serviceUsageRepository.getUsageById(usageId);
        if (!current.serviceId().equals(serviceId)) {
            throw new InvalidUsageOperationException("Usage does not belong to service: " + usageId);
        }
        if (!current.active()) {
            throw new InvalidUsageOperationException("Cannot migrate inactive usage: " + usageId);
        }
        if (Objects.equals(current.version(), targetVersion)) {
            throw new InvalidUsageOperationException("Target version must differ from current version");
        }

        validateSchema(current.subject(), targetVersion);
        ensureNoDuplicateUsage(serviceId, current.subject(), targetVersion, current.role(), null, true);

        ServiceUsageInfo migrated = serviceUsageRepository.createMigratedUsage(serviceId, usageId, targetVersion, Instant.now());
        auditService.record(auditCommand(
                current.serviceId(),
                current.serviceName(),
                current.id(),
                ServiceUsageAuditAction.USAGE_MIGRATED,
                current.subject(),
                migrated.subject(),
                current.version(),
                migrated.version(),
                current.role(),
                migrated.role(),
                true,
                true,
                current.serviceActive(),
                migrated.serviceActive(),
                resolvedChangedBy
        ));
        return migrated;
    }

    public List<ServiceUsageInfo> getUsageBySubject(String subject) {
        return serviceUsageRepository.getUsageBySubject(subject);
    }

    public List<ServiceUsageInfo> listServiceUsages(Long serviceId, Boolean active, ServiceRole role, String subject) {
        serviceUsageRepository.getServiceById(serviceId);
        return serviceUsageRepository.getServiceUsages(serviceId, active, role, subject);
    }

    public List<ServiceUsageAuditRecord> getServiceAudit(
            Long serviceId,
            ServiceUsageAuditAction action,
            Integer limit
    ) {
        serviceUsageRepository.getServiceById(serviceId);
        return auditService.getServiceAudit(serviceId, action, limit);
    }

    public List<ServiceUsageAuditRecord> getUsageAudit(Long serviceId, Long usageId, Integer limit) {
        ServiceUsageInfo usage = serviceUsageRepository.getUsageById(usageId);
        if (!usage.serviceId().equals(serviceId)) {
            throw new InvalidUsageOperationException("Usage does not belong to service: " + usageId);
        }
        return auditService.getUsageAudit(serviceId, usageId, limit);
    }

    public List<ServiceUsageAuditRecord> searchAudit(
            Long serviceId,
            Long usageId,
            ServiceUsageAuditAction action,
            Integer limit
    ) {
        if (serviceId != null) {
            serviceUsageRepository.getServiceById(serviceId);
        }
        if (usageId != null) {
            serviceUsageRepository.getUsageById(usageId);
        }
        return auditService.searchAudit(serviceId, usageId, action, limit);
    }

    public ServiceUsageInfo updateUsageStatus(Long usageId, UsageStatus targetStatus) {
        ServiceUsageInfo currentUsage = serviceUsageRepository.getUsageById(usageId);

        if (currentUsage.status() == targetStatus) {
            return currentUsage;
        }

        validateTransition(currentUsage.status(), targetStatus);

        boolean active = targetStatus == UsageStatus.ACTIVE;
        Instant activeTo = switch (targetStatus) {
            case ACTIVE -> null;
            case MIGRATING, DEPRECATED -> currentUsage.activeTo() != null ? currentUsage.activeTo() : Instant.now();
        };

        return serviceUsageRepository.updateUsageStatus(
                new UpdateServiceUsageStatusCommand(
                        usageId,
                        targetStatus,
                        active,
                        activeTo
                )
        );
    }

    public List<ServiceUsageInfo> getActiveConsumers(String subject, Integer version) {
        return serviceUsageRepository.getActiveConsumers(subject, version);
    }

    public List<ServiceUsageInfo> getActiveProducers(String subject, Integer version) {
        return serviceUsageRepository.getActiveProducers(subject, version);
    }

    private void ensureNoDuplicateUsage(
            Long serviceId,
            String subject,
            Integer version,
            ServiceRole role,
            Long excludeUsageId,
            boolean active
    ) {
        if (!active) {
            return;
        }
        if (serviceUsageRepository.hasActiveDuplicateUsage(serviceId, subject, version, role, excludeUsageId)) {
            throw new ServiceUsageAlreadyExistsException(serviceId, subject, version, role.name());
        }
    }

    private void validateTransition(UsageStatus currentStatus, UsageStatus targetStatus) {
        boolean valid = switch (currentStatus) {
            case ACTIVE -> targetStatus == UsageStatus.MIGRATING || targetStatus == UsageStatus.DEPRECATED;
            case MIGRATING -> targetStatus == UsageStatus.DEPRECATED;
            case DEPRECATED -> false;
        };

        if (!valid) {
            throw new InvalidUsageOperationException(
                    "Usage status transition is not allowed: " + currentStatus + " -> " + targetStatus
            );
        }
    }

    private void validateSchema(String subject, Integer version) {
        String normalizedSubject = normalizeSubject(subject);
        try {
            schemaCatalog.listVersions(normalizedSubject);
        } catch (ResourceNotFoundException exception) {
            throw new SchemaSubjectNotFoundException(normalizedSubject);
        }

        if (version == null) {
            return;
        }

        try {
            schemaCatalog.getVersion(normalizedSubject, version);
        } catch (ResourceNotFoundException exception) {
            throw new SchemaVersionNotFoundException(normalizedSubject, version);
        }
    }

    private ServiceUsageAuditCommand usageAuditDeactivated(ServiceUsageInfo usage, String changedBy, boolean newServiceActive) {
        return auditCommand(
                usage.serviceId(),
                usage.serviceName(),
                usage.id(),
                ServiceUsageAuditAction.USAGE_DEACTIVATED,
                usage.subject(),
                usage.subject(),
                usage.version(),
                usage.version(),
                usage.role(),
                usage.role(),
                usage.active(),
                false,
                usage.serviceActive(),
                newServiceActive,
                changedBy
        );
    }

    private ServiceUsageAuditCommand auditCommand(
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
            String changedBy
    ) {
        return new ServiceUsageAuditCommand(
                serviceId,
                serviceName,
                usageId,
                action,
                oldSubject,
                newSubject,
                oldVersion,
                newVersion,
                oldRole,
                newRole,
                oldActive,
                newActive,
                oldServiceActive,
                newServiceActive,
                normalizeChangedBy(changedBy),
                null,
                Instant.now()
        );
    }

    private String normalizeChangedBy(String value) {
        if (value == null || value.isBlank()) {
            return "system";
        }
        return value.trim();
    }

    private String resolveActor(String fallbackActor) {
        return currentUserService.currentUsernameOptional()
                .orElseGet(() -> normalizeChangedBy(fallbackActor));
    }

    private String normalizeName(String name) {
        if (name == null) {
            return null;
        }
        String normalized = name.trim();
        if (normalized.isEmpty()) {
            throw new InvalidUsageOperationException("Service name must not be blank");
        }
        return normalized;
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeSubject(String subject) {
        if (subject == null) {
            return null;
        }
        String normalized = subject.trim();
        if (normalized.isEmpty()) {
            throw new InvalidUsageOperationException("Subject must not be blank");
        }
        return normalized;
    }
}
