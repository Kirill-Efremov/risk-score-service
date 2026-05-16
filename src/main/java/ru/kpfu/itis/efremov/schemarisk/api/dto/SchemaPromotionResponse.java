package ru.kpfu.itis.efremov.schemarisk.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.kpfu.itis.efremov.schemarisk.analysis.governance.SchemaPromotionStatus;

@Schema(description = "Ответ controlled promotion flow")
public record SchemaPromotionResponse(
        @Schema(description = "Имя subject", example = "user-created")
        String subject,
        @Schema(description = "Флаг успешной регистрации схемы в Schema Registry", example = "true")
        boolean registered,
        @Schema(description = "Базовая latest-версия, относительно которой выполнялся анализ", example = "3")
        Integer oldVersion,
        @Schema(description = "Версия, зарегистрированная в Schema Registry", example = "4")
        Integer registeredVersion,
        @Schema(description = "Идентификатор схемы в Schema Registry", example = "25")
        Integer schemaRegistryId,
        @Schema(description = "Статус controlled promotion flow")
        SchemaPromotionStatus registrationStatus,
        @Schema(description = "Пояснение результата регистрации")
        String registrationMessage,
        @Schema(description = "Полный результат анализа, на основе которого было принято решение")
        SchemaAnalysisResponse analysis
) {
}
