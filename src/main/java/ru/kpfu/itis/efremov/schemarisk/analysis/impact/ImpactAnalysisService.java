package ru.kpfu.itis.efremov.schemarisk.analysis.impact;

import org.springframework.stereotype.Service;
import ru.kpfu.itis.efremov.schemarisk.analysis.impact.ImpactResult;
import ru.kpfu.itis.efremov.schemarisk.common.port.ServiceUsageRepository;
import ru.kpfu.itis.efremov.schemarisk.usage.model.ServiceUsageInfo;
import ru.kpfu.itis.efremov.schemarisk.analysis.compatibility.CompatibilityResult;

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




