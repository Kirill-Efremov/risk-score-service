package ru.kpfu.itis.efremov.schemarisk.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import ru.kpfu.itis.efremov.schemarisk.analysis.compatibility.CompatibilityResult;
import ru.kpfu.itis.efremov.schemarisk.analysis.diff.DiffResult;
import ru.kpfu.itis.efremov.schemarisk.analysis.governance.GovernanceDecision;
import ru.kpfu.itis.efremov.schemarisk.analysis.governance.StructuredRecommendation;
import ru.kpfu.itis.efremov.schemarisk.analysis.graph.dto.UsageGraphResponse;
import ru.kpfu.itis.efremov.schemarisk.analysis.impact.ImpactResult;
import ru.kpfu.itis.efremov.schemarisk.analysis.model.SchemaAnalysisResult;
import ru.kpfu.itis.efremov.schemarisk.analysis.risk.RiskResult;
import ru.kpfu.itis.efremov.schemarisk.common.model.Issue;

import java.util.List;
import java.util.Objects;

@Data
@Builder
@Schema(description = "Результат анализа изменения схемы")
public class SchemaAnalysisResponse {

    @Schema(description = "Флаг формальной совместимости схем", example = "true")
    private boolean compatible;

    @Schema(description = "Режим совместимости, в котором выполнялся анализ", example = "BACKWARD")
    private String mode;

    @Schema(description = "Список проблем совместимости")
    private List<Issue> issues;

    @Schema(description = "Результат diff-анализа схем")
    private DiffResult diff;

    @Schema(description = "Числовая оценка риска", example = "65")
    private int riskScore;

    @Schema(description = "Уровень риска", example = "MEDIUM")
    private String riskLevel;

    @Schema(
            description = "Техническое решение на основе riskLevel. Возможные значения: ALLOW / WARN / BLOCK.",
            example = "WARN"
    )
    private String decision;

    @Schema(
            description = "Предметное governance-решение с учетом контекста и policy. "
                    + "В raw-analysis может быть null.",
            example = "ALLOW_WITH_CAUTION"
    )
    private String governanceDecision;

    @Schema(description = "Факторы риска, из которых складывается итоговый riskScore")
    private List<RiskFactorResponse> riskFactors;

    @Schema(description = "Объяснение governance-решения. Для raw-analysis обычно пустой список.")
    private List<String> decisionExplanation;

    @Schema(description = "Текстовые рекомендации для человека")
    private List<String> recommendations;

    @Schema(description = "Структурированные рекомендации для CI/CD, аудита и автоматизации")
    private List<StructuredRecommendationResponse> structuredRecommendations;

    @Schema(description = "Impact-анализ зависимых producer/consumer сервисов. Для raw-analysis может быть null.")
    private ImpactResponse impact;

    @Schema(description = "Граф зависимостей и влияния. Для raw-analysis может быть null.")
    private UsageGraphResponse impactGraph;

    public static SchemaAnalysisResponse fromResult(
            CompatibilityResult result,
            DiffResult diffResult,
            RiskResult riskResult,
            GovernanceDecision governanceDecision,
            List<String> decisionExplanation,
            List<String> recommendations,
            List<StructuredRecommendation> structuredRecommendations,
            ImpactResult impactResult,
            UsageGraphResponse impactGraph
    ) {
        List<StructuredRecommendation> safeStructuredRecommendations =
                structuredRecommendations != null ? structuredRecommendations : List.of();

        return SchemaAnalysisResponse.builder()
                .compatible(result.isCompatible())
                .mode(result.getMode().name())
                .issues(Objects.requireNonNullElse(result.getIssues(), List.of()))
                .diff(diffResult)
                .riskScore(riskResult.getRiskScore())
                .riskLevel(riskResult.getRiskLevel().name())
                .decision(riskResult.getDecision().name())
                .governanceDecision(governanceDecision != null ? governanceDecision.name() : null)
                .riskFactors(riskResult.getRiskFactors().stream().map(RiskFactorResponse::fromFactor).toList())
                .decisionExplanation(Objects.requireNonNullElse(decisionExplanation, List.of()))
                .recommendations(Objects.requireNonNullElse(recommendations, List.of()))
                .structuredRecommendations(safeStructuredRecommendations.stream()
                        .map(StructuredRecommendationResponse::fromRecommendation)
                        .toList())
                .impact(ImpactResponse.fromResult(impactResult))
                .impactGraph(impactGraph)
                .build();
    }

    public static SchemaAnalysisResponse fromResult(SchemaAnalysisResult result) {
        return fromResult(
                result.compatibilityResult(),
                result.diffResult(),
                result.riskResult(),
                result.governanceDecision(),
                result.decisionExplanation(),
                result.recommendations(),
                result.structuredRecommendations(),
                result.impact(),
                result.impactGraph()
        );
    }
}
