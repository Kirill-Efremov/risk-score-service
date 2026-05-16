package ru.kpfu.itis.efremov.schemarisk.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.kpfu.itis.efremov.schemarisk.analysis.diff.DiffResult;
import ru.kpfu.itis.efremov.schemarisk.analysis.graph.dto.UsageGraphResponse;
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
        @Schema(description = "Техническое решение на основе riskLevel", example = "WARN")
        String decision,
        @Schema(description = "Governance-решение с учетом policy и контекста rollout", example = "REQUIRE_CONSUMER_UPGRADE_FIRST")
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
        @Schema(description = "Граф влияния и зависимостей")
        UsageGraphResponse impactGraph,
        @Schema(description = "Текст исходной схемы для diff viewer")
        String oldSchemaText,
        @Schema(description = "Текст новой схемы для diff viewer")
        String newSchemaText,
        @Schema(description = "Флаг, что анализ был выполнен в рамках controlled promotion flow")
        Boolean promotionAttempted,
        @Schema(description = "Флаг, что схема была зарегистрирована в Schema Registry")
        Boolean registered,
        @Schema(description = "Статус попытки публикации схемы")
        String registrationStatus,
        @Schema(description = "Номер версии, зарегистрированной в Schema Registry")
        Integer registeredVersion,
        @Schema(description = "ID схемы в Schema Registry")
        Integer schemaRegistryId,
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
                record.impactGraph(),
                record.oldSchemaText(),
                record.newSchemaText(),
                record.promotionAttempted(),
                record.registered(),
                record.registrationStatus() != null ? record.registrationStatus().name() : null,
                record.registeredVersion(),
                record.schemaRegistryId(),
                record.createdAt(),
                record.createdBy()
        );
    }
}
