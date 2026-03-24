package ru.kpfu.itis.efremov.schemarisk.application.usage.service;

import org.springframework.stereotype.Service;
import ru.kpfu.itis.efremov.schemarisk.application.port.ServiceUsageRepository;
import ru.kpfu.itis.efremov.schemarisk.application.usage.model.RegisterServiceCommand;
import ru.kpfu.itis.efremov.schemarisk.application.usage.model.RegisterServiceUsageCommand;
import ru.kpfu.itis.efremov.schemarisk.application.usage.model.ServiceInfo;
import ru.kpfu.itis.efremov.schemarisk.application.usage.model.ServiceUsageInfo;
import ru.kpfu.itis.efremov.schemarisk.application.usage.model.UpdateServiceUsageStatusCommand;
import ru.kpfu.itis.efremov.schemarisk.application.usage.model.UsageStatus;
import ru.kpfu.itis.efremov.schemarisk.support.exception.InvalidRequestException;

import java.time.Instant;
import java.util.List;

@Service
public class ServiceUsageService {

    private final ServiceUsageRepository serviceUsageRepository;

    public ServiceUsageService(ServiceUsageRepository serviceUsageRepository) {
        this.serviceUsageRepository = serviceUsageRepository;
    }

    public ServiceInfo registerService(RegisterServiceCommand command) {
        return serviceUsageRepository.registerService(command);
    }

    public ServiceUsageInfo registerUsage(RegisterServiceUsageCommand command) {
        return serviceUsageRepository.registerUsage(command);
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

    public List<ServiceUsageInfo> getUsageBySubject(String subject) {
        return serviceUsageRepository.getUsageBySubject(subject);
    }

    public List<ServiceUsageInfo> getActiveConsumers(String subject, Integer version) {
        return serviceUsageRepository.getActiveConsumers(subject, version);
    }

    public List<ServiceUsageInfo> getActiveProducers(String subject, Integer version) {
        return serviceUsageRepository.getActiveProducers(subject, version);
    }

    private void validateTransition(UsageStatus currentStatus, UsageStatus targetStatus) {
        boolean valid = switch (currentStatus) {
            case ACTIVE -> targetStatus == UsageStatus.MIGRATING || targetStatus == UsageStatus.DEPRECATED;
            case MIGRATING -> targetStatus == UsageStatus.DEPRECATED;
            case DEPRECATED -> false;
        };

        if (!valid) {
            throw new InvalidRequestException(
                    "Usage status transition is not allowed: " + currentStatus + " -> " + targetStatus
            );
        }
    }
}
