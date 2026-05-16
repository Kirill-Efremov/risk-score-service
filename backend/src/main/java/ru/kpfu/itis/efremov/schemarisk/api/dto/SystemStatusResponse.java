package ru.kpfu.itis.efremov.schemarisk.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Сводный статус backend и зависимостей")
public record SystemStatusResponse(
        @Schema(example = "UP")
        String backend,
        @Schema(example = "UP")
        String schemaRegistry,
        @Schema(example = "UP")
        String database
) {
}
