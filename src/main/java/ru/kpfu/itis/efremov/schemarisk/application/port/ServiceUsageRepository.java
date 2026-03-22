package ru.kpfu.itis.efremov.schemarisk.application.port;

import ru.kpfu.itis.efremov.schemarisk.application.usage.model.RegisterServiceCommand;
import ru.kpfu.itis.efremov.schemarisk.application.usage.model.RegisterServiceUsageCommand;
import ru.kpfu.itis.efremov.schemarisk.application.usage.model.ServiceInfo;
import ru.kpfu.itis.efremov.schemarisk.application.usage.model.ServiceUsageInfo;

import java.util.List;

public interface ServiceUsageRepository {

    ServiceInfo registerService(RegisterServiceCommand command);

    ServiceUsageInfo registerUsage(RegisterServiceUsageCommand command);

    List<ServiceUsageInfo> getUsageBySubject(String subject);

    List<ServiceUsageInfo> getActiveConsumersBySubject(String subject);

    List<ServiceUsageInfo> getActiveProducersBySubject(String subject);

    List<ServiceUsageInfo> getActiveConsumers(String subject, Integer version);

    List<ServiceUsageInfo> getActiveProducers(String subject, Integer version);
}
