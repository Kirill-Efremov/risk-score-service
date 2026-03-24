package ru.kpfu.itis.efremov.schemarisk.infrastructure.usage;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.kpfu.itis.efremov.schemarisk.application.port.ServiceUsageRepository;
import ru.kpfu.itis.efremov.schemarisk.application.usage.model.RegisterServiceCommand;
import ru.kpfu.itis.efremov.schemarisk.application.usage.model.RegisterServiceUsageCommand;
import ru.kpfu.itis.efremov.schemarisk.application.usage.model.ServiceInfo;
import ru.kpfu.itis.efremov.schemarisk.application.usage.model.ServiceRole;
import ru.kpfu.itis.efremov.schemarisk.application.usage.model.ServiceUsageInfo;
import ru.kpfu.itis.efremov.schemarisk.application.usage.model.UpdateServiceUsageStatusCommand;
import ru.kpfu.itis.efremov.schemarisk.application.usage.model.UsageStatus;
import ru.kpfu.itis.efremov.schemarisk.infrastructure.usage.persistence.entity.ServiceEntity;
import ru.kpfu.itis.efremov.schemarisk.infrastructure.usage.persistence.entity.ServiceSchemaUsageEntity;
import ru.kpfu.itis.efremov.schemarisk.infrastructure.usage.persistence.repository.ServiceEntityRepository;
import ru.kpfu.itis.efremov.schemarisk.infrastructure.usage.persistence.repository.ServiceSchemaUsageJpaRepository;
import ru.kpfu.itis.efremov.schemarisk.support.exception.InvalidRequestException;
import ru.kpfu.itis.efremov.schemarisk.support.exception.ResourceNotFoundException;

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
            throw new InvalidRequestException("Service already exists: " + command.name());
        });

        ServiceEntity entity = new ServiceEntity();
        entity.setName(command.name());
        entity.setCritical(command.critical());
        entity.setCreatedAt(Instant.now());
        return serviceUsageMapper.toServiceInfo(serviceEntityRepository.save(entity));
    }

    @Override
    @Transactional
    public ServiceUsageInfo registerUsage(RegisterServiceUsageCommand command) {
        ServiceEntity service = serviceEntityRepository.findById(command.serviceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service not found: " + command.serviceId()));

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
        entity.setActiveFrom(now);
        entity.setActiveTo(active ? null : now);
        return serviceUsageMapper.toUsageInfo(serviceSchemaUsageJpaRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceUsageInfo getUsageById(Long usageId) {
        return serviceUsageMapper.toUsageInfo(
                serviceSchemaUsageJpaRepository.findById(usageId)
                        .orElseThrow(() -> new ResourceNotFoundException("Usage not found: " + usageId))
        );
    }

    @Override
    @Transactional
    public ServiceUsageInfo updateUsageStatus(UpdateServiceUsageStatusCommand command) {
        ServiceSchemaUsageEntity entity = serviceSchemaUsageJpaRepository.findById(command.usageId())
                .orElseThrow(() -> new ResourceNotFoundException("Usage not found: " + command.usageId()));

        entity.setStatus(command.status());
        entity.setActive(command.active());
        entity.setActiveTo(command.activeTo());

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
}
