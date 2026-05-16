package ru.kpfu.itis.efremov.schemarisk.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.kpfu.itis.efremov.schemarisk.analysis.diff.DiffResult;
import ru.kpfu.itis.efremov.schemarisk.common.model.Issue;
import ru.kpfu.itis.efremov.schemarisk.history.model.AnalysisRecord;

import java.time.Instant;
import java.util.List;

@Schema(description = "Сохраненный результат анализа схемы")
public record AnalysisRecordResponse(
        @Schema(description = "Идентификатор анализа", example = "42")
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
        @Schema(description = "Список обнаруженных проблем совместимости")
        List<Issue> issues,
        @Schema(description = "Diff схем")
        DiffResult diff,
        @Schema(description = "Числовая оценка риска", example = "65")
        int riskScore,
        @Schema(description = "Уровень риска", example = "MEDIUM")
        String riskLevel,
        @Schema(
                description = "Техническое решение на основе riskLevel. Возможные значения: ALLOW / WARN / BLOCK.",
                example = "WARN"
        )
        String decision,
        @Schema(
                description = "Предметное governance-решение с учетом policy и контекста rollout.",
                example = "REQUIRE_CONSUMER_UPGRADE_FIRST"
        )
        String governanceDecision,
        @Schema(description = "Объяснение governance-решения")
        List<String> decisionExplanation,
        @Schema(description = "Факторы риска, повлиявшие на итоговый riskScore")
        List<RiskFactorResponse> riskFactors,
        @Schema(description = "Текстовые рекомендации")
        List<String> recommendations,
        @Schema(description = "Структурированные рекомендации для снижения риска")
        List<StructuredRecommendationResponse> structuredRecommendations,
        @Schema(description = "Impact-анализ зависимых сервисов")
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
                record.governanceDecision() != null ? record.governanceDecision().name() : null,
                record.decisionExplanation(),
                record.riskFactors().stream().map(RiskFactorResponse::fromFactor).toList(),
                record.recommendations(),
                record.structuredRecommendations().stream()
                        .map(StructuredRecommendationResponse::fromRecommendation)
                        .toList(),
                ImpactResponse.fromResult(record.impact()),
                record.createdAt(),
                record.createdBy()
        );
    }
}
