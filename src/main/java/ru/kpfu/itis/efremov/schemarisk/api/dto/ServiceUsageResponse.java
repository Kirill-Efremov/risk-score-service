package ru.kpfu.itis.efremov.schemarisk.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.kpfu.itis.efremov.schemarisk.usage.model.ServiceUsageInfo;

import java.time.Instant;

@Schema(description = "Информация об использовании схемы сервисом")
public record ServiceUsageResponse(
        @Schema(description = "ID usage", example = "101")
        Long id,
        @Schema(description = "ID сервиса", example = "10")
        Long serviceId,
        @Schema(description = "Имя сервиса", example = "billing-service")
        String serviceName,
        @Schema(description = "Признак критичности сервиса", example = "true")
        boolean critical,
        @Schema(description = "Имя subject", example = "user-created")
        String subject,
        @Schema(description = "Версия схемы", example = "2")
        Integer version,
        @Schema(description = "Роль сервиса", example = "CONSUMER")
        String role,
        @Schema(description = "Lifecycle-статус usage", example = "ACTIVE")
        String status,
        @Schema(description = "Признак активности usage", example = "true")
        boolean active,
        @Schema(description = "Время создания")
        Instant createdAt,
        @Schema(description = "Время начала активности")
        Instant activeFrom,
        @Schema(description = "Время окончания активности")
        Instant activeTo
) {
    public static ServiceUsageResponse fromInfo(ServiceUsageInfo info) {
        return new ServiceUsageResponse(
                info.id(),
                info.serviceId(),
                info.serviceName(),
                info.critical(),
                info.subject(),
                info.version(),
                info.role().name(),
                info.status().name(),
                info.active(),
                info.createdAt(),
                info.activeFrom(),
                info.activeTo()
        );
    }
}




