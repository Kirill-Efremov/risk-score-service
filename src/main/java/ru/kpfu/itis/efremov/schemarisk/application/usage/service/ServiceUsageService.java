package ru.kpfu.itis.efremov.schemarisk.application.usage.service;

import org.springframework.stereotype.Service;
import ru.kpfu.itis.efremov.schemarisk.application.port.ServiceUsageRepository;
import ru.kpfu.itis.efremov.schemarisk.application.usage.model.RegisterServiceCommand;
import ru.kpfu.itis.efremov.schemarisk.application.usage.model.RegisterServiceUsageCommand;
import ru.kpfu.itis.efremov.schemarisk.application.usage.model.ServiceInfo;
import ru.kpfu.itis.efremov.schemarisk.application.usage.model.ServiceUsageInfo;

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

    public List<ServiceUsageInfo> getUsageBySubject(String subject) {
        return serviceUsageRepository.getUsageBySubject(subject);
    }

    public List<ServiceUsageInfo> getActiveConsumers(String subject, Integer version) {
        return serviceUsageRepository.getActiveConsumers(subject, version);
    }

    public List<ServiceUsageInfo> getActiveProducers(String subject, Integer version) {
        return serviceUsageRepository.getActiveProducers(subject, version);
    }
}
