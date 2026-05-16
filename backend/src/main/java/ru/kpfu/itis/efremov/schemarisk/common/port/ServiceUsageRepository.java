package ru.kpfu.itis.efremov.schemarisk.common.port;

import ru.kpfu.itis.efremov.schemarisk.usage.model.RegisterServiceCommand;
import ru.kpfu.itis.efremov.schemarisk.usage.model.RegisterServiceUsageCommand;
import ru.kpfu.itis.efremov.schemarisk.usage.model.ServiceInfo;
import ru.kpfu.itis.efremov.schemarisk.usage.model.ServiceUsageInfo;
import ru.kpfu.itis.efremov.schemarisk.usage.model.UpdateServiceUsageStatusCommand;

import java.util.List;

public interface ServiceUsageRepository {

    ServiceInfo registerService(RegisterServiceCommand command);

    ServiceUsageInfo registerUsage(RegisterServiceUsageCommand command);

    ServiceUsageInfo getUsageById(Long usageId);

    ServiceUsageInfo updateUsageStatus(UpdateServiceUsageStatusCommand command);

    List<ServiceUsageInfo> getUsageBySubject(String subject);

    List<ServiceUsageInfo> getActiveConsumersBySubject(String subject);

    List<ServiceUsageInfo> getActiveProducersBySubject(String subject);

    List<ServiceUsageInfo> getActiveConsumers(String subject, Integer version);

    List<ServiceUsageInfo> getActiveProducers(String subject, Integer version);
}




