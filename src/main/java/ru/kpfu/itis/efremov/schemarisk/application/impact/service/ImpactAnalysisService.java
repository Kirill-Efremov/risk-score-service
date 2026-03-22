package ru.kpfu.itis.efremov.schemarisk.application.impact.service;

import org.springframework.stereotype.Service;
import ru.kpfu.itis.efremov.schemarisk.application.impact.model.ImpactResult;
import ru.kpfu.itis.efremov.schemarisk.application.port.ServiceUsageRepository;
import ru.kpfu.itis.efremov.schemarisk.application.usage.model.ServiceUsageInfo;
import ru.kpfu.itis.efremov.schemarisk.core.engine.CompatibilityResult;

import java.util.List;

@Service
public class ImpactAnalysisService {

    private final ServiceUsageRepository serviceUsageRepository;

    public ImpactAnalysisService(ServiceUsageRepository serviceUsageRepository) {
        this.serviceUsageRepository = serviceUsageRepository;
    }

    public ImpactResult analyze(
            String subject,
            Integer oldVersion,
            Integer newVersion,
            CompatibilityResult compatibilityResult
    ) {
        List<ServiceUsageInfo> consumers = serviceUsageRepository.getActiveConsumersBySubject(subject);
        List<ServiceUsageInfo> producers = serviceUsageRepository.getActiveProducersBySubject(subject);
        List<String> criticalServices = java.util.stream.Stream.concat(consumers.stream(), producers.stream())
                .filter(ServiceUsageInfo::critical)
                .map(ServiceUsageInfo::serviceName)
                .distinct()
                .sorted()
                .toList();

        return new ImpactResult(
                consumers.size(),
                producers.size(),
                criticalServices,
                !compatibilityResult.isCompatible()
        );
    }
}
