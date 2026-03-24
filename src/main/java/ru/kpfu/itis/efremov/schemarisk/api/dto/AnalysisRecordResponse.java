package ru.kpfu.itis.efremov.schemarisk.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.kpfu.itis.efremov.schemarisk.history.model.AnalysisRecord;

import java.time.Instant;
import java.util.List;

@Schema(description = "Сохранённый результат анализа схемы")
public record AnalysisRecordResponse(
        @Schema(description = "ID анализа", example = "42")
        Long id,
        @Schema(description = "Имя subject", example = "user-created")
        String subject,
        @Schema(description = "Старая версия схемы", example = "1")
        Integer oldVersion,
        @Schema(description = "Новая версия схемы", example = "2")
        Integer newVersion,
        @Schema(description = "Режим совместимости", example = "BACKWARD")
        String compatibilityMode,
        @Schema(description = "Флаг формальной совместимости", example = "false")
        boolean formalCompatible,
        @Schema(description = "Список обнаруженных проблем")
        List<?> issues,
        @Schema(description = "Diff схем")
        Object diff,
        @Schema(description = "Числовая оценка риска", example = "65")
        int riskScore,
        @Schema(description = "Уровень риска", example = "MEDIUM")
        String riskLevel,
        @Schema(description = "Итоговое решение", example = "REJECT")
        String decision,
        @Schema(description = "Рекомендации")
        List<String> recommendations,
        @Schema(description = "Impact-анализ")
        ImpactResponse impact,
        @Schema(description = "Время создания записи")
        Instant createdAt,
        @Schema(description = "Кто инициировал анализ")
        String createdBy
) {
    public static AnalysisRecordResponse fromRecord(AnalysisRecord record) {
        return new AnalysisRecordResponse(
                record.id(),
                record.subjectName(),
                record.oldVersion(),
                record.newVersion(),
                record.compatibilityMode().name(),
                record.formalCompatible(),
                record.issues(),
                record.diff(),
                record.riskScore(),
                record.riskLevel().name(),
                record.decision().name(),
                record.recommendations(),
                ImpactResponse.fromResult(record.impact()),
                record.createdAt(),
                record.createdBy()
        );
    }
}




