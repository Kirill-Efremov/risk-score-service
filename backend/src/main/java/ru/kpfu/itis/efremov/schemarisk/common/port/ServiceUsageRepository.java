package ru.kpfu.itis.efremov.schemarisk.common.port;

import ru.kpfu.itis.efremov.schemarisk.usage.model.RegisterServiceCommand;
import ru.kpfu.itis.efremov.schemarisk.usage.model.RegisterServiceUsageCommand;
import ru.kpfu.itis.efremov.schemarisk.usage.model.ServiceInfo;
import ru.kpfu.itis.efremov.schemarisk.usage.model.ServiceRole;
import ru.kpfu.itis.efremov.schemarisk.usage.model.ServiceUsageInfo;
import ru.kpfu.itis.efremov.schemarisk.usage.model.UpdateServiceUsageStatusCommand;

import java.time.Instant;
import java.util.List;

public interface ServiceUsageRepository {

    ServiceInfo registerService(RegisterServiceCommand command);

    ServiceUsageInfo registerUsage(RegisterServiceUsageCommand command);

    List<ServiceInfo> listServices(Boolean active, Boolean critical);

    ServiceInfo getServiceById(Long serviceId);

    ServiceInfo updateService(
            Long serviceId,
            String name,
            Boolean critical,
            Boolean active,
            String owner,
            String description,
            Instant updatedAt
    );

    ServiceInfo deactivateService(Long serviceId, Instant updatedAt);

    ServiceUsageInfo getUsageById(Long usageId);

    ServiceUsageInfo updateUsageStatus(UpdateServiceUsageStatusCommand command);

    List<ServiceUsageInfo> getUsageBySubject(String subject);

    List<ServiceUsageInfo> getServiceUsages(Long serviceId, Boolean active, ServiceRole role, String subject);

    ServiceUsageInfo updateUsage(
            Long serviceId,
            Long usageId,
            String subject,
            Integer version,
            ServiceRole role,
            Boolean active,
            Instant updatedAt
    );

    ServiceUsageInfo deactivateUsage(Long serviceId, Long usageId, Instant updatedAt);

    ServiceUsageInfo createMigratedUsage(Long serviceId, Long usageId, Integer targetVersion, Instant now);

    List<ServiceUsageInfo> getActiveConsumersBySubject(String subject);

    List<ServiceUsageInfo> getActiveProducersBySubject(String subject);

    List<ServiceUsageInfo> getActiveConsumers(String subject, Integer version);

    List<ServiceUsageInfo> getActiveProducers(String subject, Integer version);

    boolean hasActiveDuplicateUsage(
            Long serviceId,
            String subject,
            Integer version,
            ServiceRole role,
            Long excludeUsageId
    );
}
