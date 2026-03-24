package ru.kpfu.itis.efremov.schemarisk.infrastructure.usage;

import org.springframework.stereotype.Component;
import ru.kpfu.itis.efremov.schemarisk.application.usage.model.ServiceInfo;
import ru.kpfu.itis.efremov.schemarisk.application.usage.model.ServiceUsageInfo;
import ru.kpfu.itis.efremov.schemarisk.infrastructure.usage.persistence.entity.ServiceEntity;
import ru.kpfu.itis.efremov.schemarisk.infrastructure.usage.persistence.entity.ServiceSchemaUsageEntity;

@Component
public class ServiceUsageMapper {

    public ServiceInfo toServiceInfo(ServiceEntity entity) {
        return new ServiceInfo(
                entity.getId(),
                entity.getName(),
                entity.isCritical(),
                entity.getCreatedAt()
        );
    }

    public ServiceUsageInfo toUsageInfo(ServiceSchemaUsageEntity entity) {
        return new ServiceUsageInfo(
                entity.getId(),
                entity.getService().getId(),
                entity.getService().getName(),
                entity.getService().isCritical(),
                entity.getSubject(),
                entity.getVersion(),
                entity.getRole(),
                entity.getStatus(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getActiveFrom(),
                entity.getActiveTo()
        );
    }
}
