package ru.kpfu.itis.efremov.schemarisk.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.kpfu.itis.efremov.schemarisk.analysis.impact.ImpactResult;

import java.util.List;

@Schema(description = "Результат impact-анализа")
public record ImpactResponse(
        @Schema(description = "Количество затронутых consumer-сервисов", example = "3")
        int affectedConsumersCount,
        @Schema(description = "Количество затронутых producer-сервисов", example = "1")
        int affectedProducersCount,
        @Schema(description = "Список критичных сервисов")
        List<String> criticalServices,
        @Schema(description = "Является ли изменение breaking", example = "true")
        boolean breaking
) {
    public static ImpactResponse fromResult(ImpactResult impactResult) {
        if (impactResult == null) {
            return null;
        }
        return new ImpactResponse(
                impactResult.affectedConsumersCount(),
                impactResult.affectedProducersCount(),
                impactResult.criticalServices(),
                impactResult.breaking()
        );
    }
}




