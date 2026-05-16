package ru.kpfu.itis.efremov.schemarisk.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.kpfu.itis.efremov.schemarisk.analysis.governance.StructuredRecommendation;

@Schema(description = "Структурированная рекомендация по снижению риска")
public record StructuredRecommendationResponse(
        @Schema(description = "Код рекомендации", example = "FIELD_REMOVED_USE_DEPRECATION")
        String code,
        @Schema(description = "Уровень важности рекомендации", example = "HIGH")
        String severity,
        @Schema(description = "Цель рекомендации: field, schema, consumers и т.д.", example = "address.city")
        String target,
        @Schema(description = "Краткое сообщение рекомендации", example = "Field 'address.city' was removed")
        String message,
        @Schema(description = "Рекомендуемое действие")
        String action
) {
    public static StructuredRecommendationResponse fromRecommendation(StructuredRecommendation recommendation) {
        return new StructuredRecommendationResponse(
                recommendation.code(),
                recommendation.severity().name(),
                recommendation.target(),
                recommendation.message(),
                recommendation.action()
        );
    }
}
