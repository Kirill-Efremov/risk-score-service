package ru.kpfu.itis.efremov.schemarisk.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.kpfu.itis.efremov.schemarisk.usage.model.ServiceInfo;

import java.time.Instant;

@Schema(description = "Информация о зарегистрированном сервисе")
public record ServiceResponse(
        @Schema(description = "ID сервиса", example = "10")
        Long id,
        @Schema(description = "Имя сервиса", example = "billing-service")
        String name,
        @Schema(description = "Признак критичности сервиса", example = "true")
        boolean critical,
        @Schema(description = "Признак активности сервиса", example = "true")
        boolean active,
        @Schema(description = "Владелец сервиса", example = "billing-team")
        String owner,
        @Schema(description = "Описание сервиса", example = "Handles billing events")
        String description,
        @Schema(description = "Время создания")
        Instant createdAt,
        @Schema(description = "Время последнего обновления")
        Instant updatedAt
) {
    public static ServiceResponse fromInfo(ServiceInfo info) {
        return new ServiceResponse(
                info.id(),
                info.name(),
                info.critical(),
                info.active(),
                info.owner(),
                info.description(),
                info.createdAt(),
                info.updatedAt()
        );
    }
}
