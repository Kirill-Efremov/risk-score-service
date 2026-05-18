package ru.kpfu.itis.efremov.schemarisk.usage.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.kpfu.itis.efremov.schemarisk.common.exception.ServiceAlreadyExistsException;
import ru.kpfu.itis.efremov.schemarisk.common.exception.ServiceNotFoundException;
import ru.kpfu.itis.efremov.schemarisk.common.exception.ServiceUsageNotFoundException;
import ru.kpfu.itis.efremov.schemarisk.common.port.ServiceUsageRepository;
import ru.kpfu.itis.efremov.schemarisk.usage.model.RegisterServiceCommand;
import ru.kpfu.itis.efremov.schemarisk.usage.model.RegisterServiceUsageCommand;
import ru.kpfu.itis.efremov.schemarisk.usage.model.ServiceInfo;
import ru.kpfu.itis.efremov.schemarisk.usage.model.ServiceRole;
import ru.kpfu.itis.efremov.schemarisk.usage.model.ServiceUsageInfo;
import ru.kpfu.itis.efremov.schemarisk.usage.model.UpdateServiceUsageStatusCommand;
import ru.kpfu.itis.efremov.schemarisk.usage.model.UsageStatus;
import ru.kpfu.itis.efremov.schemarisk.usage.persistence.entity.ServiceEntity;
import ru.kpfu.itis.efremov.schemarisk.usage.persistence.entity.ServiceSchemaUsageEntity;
import ru.kpfu.itis.efremov.schemarisk.usage.persistence.repository.ServiceEntityRepository;
import ru.kpfu.itis.efremov.schemarisk.usage.persistence.repository.ServiceSchemaUsageJpaRepository;

import java.time.Instant;
import java.util.List;

@Component
public class JpaServiceUsageRepository implements ServiceUsageRepository {

    private final ServiceEntityRepository serviceEntityRepository;
    private final ServiceSchemaUsageJpaRepository serviceSchemaUsageJpaRepository;
    private final ServiceUsageMapper serviceUsageMapper;

    public JpaServiceUsageRepository(
            ServiceEntityRepository serviceEntityRepository,
            ServiceSchemaUsageJpaRepository serviceSchemaUsageJpaRepository,
            ServiceUsageMapper serviceUsageMapper
    ) {
        this.serviceEntityRepository = serviceEntityRepository;
        this.serviceSchemaUsageJpaRepository = serviceSchemaUsageJpaRepository;
        this.serviceUsageMapper = serviceUsageMapper;
    }

    @Override
    @Transactional
    public ServiceInfo registerService(RegisterServiceCommand command) {
        serviceEntityRepository.findByName(command.name()).ifPresent(existing -> {
            throw new ServiceAlreadyExistsException(command.name());
        });

        Instant now = Instant.now();
        ServiceEntity entity = new ServiceEntity();
        entity.setName(command.name());
        entity.setCritical(command.critical());
        entity.setActive(true);
        entity.setOwner(normalizeOptionalText(command.owner()));
        entity.setDescription(normalizeOptionalText(command.description()));
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        return serviceUsageMapper.toServiceInfo(serviceEntityRepository.save(entity));
    }

    @Override
    @Transactional
    public ServiceUsageInfo registerUsage(RegisterServiceUsageCommand command) {
        ServiceEntity service = getServiceEntity(command.serviceId());

        ServiceSchemaUsageEntity entity = new ServiceSchemaUsageEntity();
        entity.setService(service);
        entity.setSubject(command.subject());
        entity.setVersion(command.version());
        entity.setRole(command.role());
        Instant now = Instant.now();
        boolean active = command.active() == null || command.active();
        entity.setStatus(active ? UsageStatus.ACTIVE : UsageStatus.DEPRECATED);
        entity.setActive(active);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setActiveFrom(now);
        entity.setActiveTo(active ? null : now);
        return serviceUsageMapper.toUsageInfo(serviceSchemaUsageJpaRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceInfo> listServices(Boolean active, Boolean critical) {
        return serviceEntityRepository.findAllByFilters(active, critical).stream()
                .map(serviceUsageMapper::toServiceInfo)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceInfo getServiceById(Long serviceId) {
        return serviceUsageMapper.toServiceInfo(getServiceEntity(serviceId));
    }

    @Override
    @Transactional
    public ServiceInfo updateService(
            Long serviceId,
            String name,
            Boolean critical,
            Boolean active,
            String owner,
            String description,
            Instant updatedAt
    ) {
        ServiceEntity entity = getServiceEntity(serviceId);

        if (name != null && !name.equals(entity.getName())) {
            serviceEntityRepository.findByName(name)
                    .filter(existing -> !existing.getId().equals(serviceId))
                    .ifPresent(existing -> {
                        throw new ServiceAlreadyExistsException(name);
                    });
            entity.setName(name);
        }
        if (critical != null) {
            entity.setCritical(critical);
        }
        if (active != null && !active.equals(entity.isActive())) {
            entity.setActive(active);
            if (!active) {
                serviceSchemaUsageJpaRepository.deactivateActiveUsagesByServiceId(
                        serviceId,
                        UsageStatus.DEPRECATED,
                        updatedAt
                );
            }
        }
        if (owner != null) {
            entity.setOwner(normalizeOptionalText(owner));
        }
        if (description != null) {
            entity.setDescription(normalizeOptionalText(description));
        }
        entity.setUpdatedAt(updatedAt);

        return serviceUsageMapper.toServiceInfo(serviceEntityRepository.save(entity));
    }

    @Override
    @Transactional
    public ServiceInfo deactivateService(Long serviceId, Instant updatedAt) {
        ServiceEntity entity = getServiceEntity(serviceId);
        entity.setActive(false);
        entity.setUpdatedAt(updatedAt);
        serviceSchemaUsageJpaRepository.deactivateActiveUsagesByServiceId(
                serviceId,
                UsageStatus.DEPRECATED,
                updatedAt
        );
        return serviceUsageMapper.toServiceInfo(serviceEntityRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceUsageInfo getUsageById(Long usageId) {
        return serviceUsageMapper.toUsageInfo(getUsageEntity(usageId));
    }

    @Override
    @Transactional
    public ServiceUsageInfo updateUsageStatus(UpdateServiceUsageStatusCommand command) {
        ServiceSchemaUsageEntity entity = getUsageEntity(command.usageId());

        entity.setStatus(command.status());
        entity.setActive(command.active());
        entity.setActiveTo(command.activeTo());
        entity.setUpdatedAt(Instant.now());

        return serviceUsageMapper.toUsageInfo(serviceSchemaUsageJpaRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceUsageInfo> getUsageBySubject(String subject) {
        return serviceSchemaUsageJpaRepository.findAllBySubjectOrderByCreatedAtDesc(subject).stream()
                .map(serviceUsageMapper::toUsageInfo)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceUsageInfo> getServiceUsages(Long serviceId, Boolean active, ServiceRole role, String subject) {
        ensureServiceExists(serviceId);
        return serviceSchemaUsageJpaRepository.findServiceUsages(serviceId, active, role, subject).stream()
                .map(serviceUsageMapper::toUsageInfo)
                .toList();
    }

    @Override
    @Transactional
    public ServiceUsageInfo updateUsage(
            Long serviceId,
            Long usageId,
            String subject,
            Integer version,
            ServiceRole role,
            Boolean active,
            Instant updatedAt
    ) {
        ServiceSchemaUsageEntity entity = getUsageEntityForService(serviceId, usageId);

        if (subject != null) {
            entity.setSubject(subject);
        }
        if (version != null) {
            entity.setVersion(version);
        }
        if (role != null) {
            entity.setRole(role);
        }
        if (active != null && !active.equals(entity.isActive())) {
            entity.setActive(active);
            entity.setStatus(active ? UsageStatus.ACTIVE : UsageStatus.DEPRECATED);
            entity.setActiveFrom(active ? updatedAt : entity.getActiveFrom());
            entity.setActiveTo(active ? null : updatedAt);
        }
        entity.setUpdatedAt(updatedAt);

        return serviceUsageMapper.toUsageInfo(serviceSchemaUsageJpaRepository.save(entity));
    }

    @Override
    @Transactional
    public ServiceUsageInfo deactivateUsage(Long serviceId, Long usageId, Instant updatedAt) {
        ServiceSchemaUsageEntity entity = getUsageEntityForService(serviceId, usageId);
        entity.setActive(false);
        entity.setStatus(UsageStatus.DEPRECATED);
        entity.setActiveTo(updatedAt);
        entity.setUpdatedAt(updatedAt);
        return serviceUsageMapper.toUsageInfo(serviceSchemaUsageJpaRepository.save(entity));
    }

    @Override
    @Transactional
    public ServiceUsageInfo createMigratedUsage(Long serviceId, Long usageId, Integer targetVersion, Instant now) {
        ServiceSchemaUsageEntity current = getUsageEntityForService(serviceId, usageId);

        current.setActive(false);
        current.setStatus(UsageStatus.DEPRECATED);
        current.setActiveTo(now);
        current.setUpdatedAt(now);
        serviceSchemaUsageJpaRepository.save(current);

        ServiceSchemaUsageEntity migrated = new ServiceSchemaUsageEntity();
        migrated.setService(current.getService());
        migrated.setSubject(current.getSubject());
        migrated.setVersion(targetVersion);
        migrated.setRole(current.getRole());
        migrated.setStatus(UsageStatus.ACTIVE);
        migrated.setActive(true);
        migrated.setCreatedAt(now);
        migrated.setUpdatedAt(now);
        migrated.setActiveFrom(now);
        migrated.setActiveTo(null);

        return serviceUsageMapper.toUsageInfo(serviceSchemaUsageJpaRepository.save(migrated));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceUsageInfo> getActiveConsumersBySubject(String subject) {
        return serviceSchemaUsageJpaRepository.findActiveBySubjectAndRole(
                        subject,
                        ServiceRole.CONSUMER,
                        UsageStatus.ACTIVE
                ).stream()
                .map(serviceUsageMapper::toUsageInfo)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceUsageInfo> getActiveProducersBySubject(String subject) {
        return serviceSchemaUsageJpaRepository.findActiveBySubjectAndRole(
                        subject,
                        ServiceRole.PRODUCER,
                        UsageStatus.ACTIVE
                ).stream()
                .map(serviceUsageMapper::toUsageInfo)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceUsageInfo> getActiveConsumers(String subject, Integer version) {
        return serviceSchemaUsageJpaRepository.findActiveBySubjectAndRoleAndVersion(
                        subject,
                        ServiceRole.CONSUMER,
                        UsageStatus.ACTIVE,
                        version
                ).stream()
                .map(serviceUsageMapper::toUsageInfo)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceUsageInfo> getActiveProducers(String subject, Integer version) {
        return serviceSchemaUsageJpaRepository.findActiveBySubjectAndRoleAndVersion(
                        subject,
                        ServiceRole.PRODUCER,
                        UsageStatus.ACTIVE,
                        version
                ).stream()
                .map(serviceUsageMapper::toUsageInfo)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasActiveDuplicateUsage(
            Long serviceId,
            String subject,
            Integer version,
            ServiceRole role,
            Long excludeUsageId
    ) {
        return serviceSchemaUsageJpaRepository.existsActiveDuplicate(
                serviceId,
                subject,
                version,
                role,
                excludeUsageId
        );
    }

    private void ensureServiceExists(Long serviceId) {
        getServiceEntity(serviceId);
    }

    private ServiceEntity getServiceEntity(Long serviceId) {
        return serviceEntityRepository.findById(serviceId)
                .orElseThrow(() -> new ServiceNotFoundException(serviceId));
    }

    private ServiceSchemaUsageEntity getUsageEntity(Long usageId) {
        return serviceSchemaUsageJpaRepository.findById(usageId)
                .orElseThrow(() -> new ServiceUsageNotFoundException(usageId));
    }

    private ServiceSchemaUsageEntity getUsageEntityForService(Long serviceId, Long usageId) {
        ServiceSchemaUsageEntity entity = getUsageEntity(usageId);
        if (!entity.getService().getId().equals(serviceId)) {
            throw new ServiceUsageNotFoundException(usageId);
        }
        return entity;
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
