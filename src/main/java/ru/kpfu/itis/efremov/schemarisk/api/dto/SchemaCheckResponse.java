package ru.kpfu.itis.efremov.schemarisk.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import ru.kpfu.itis.efremov.schemarisk.application.impact.model.ImpactResult;
import ru.kpfu.itis.efremov.schemarisk.application.recommendation.model.GovernanceDecision;
import ru.kpfu.itis.efremov.schemarisk.application.usecase.AnalyzeSchemaChangeResult;
import ru.kpfu.itis.efremov.schemarisk.core.diff.DiffResult;
import ru.kpfu.itis.efremov.schemarisk.core.engine.CompatibilityResult;
import ru.kpfu.itis.efremov.schemarisk.core.risk.RiskResult;
import ru.kpfu.itis.efremov.schemarisk.model.Issue;

import java.util.List;
import java.util.Objects;

@Data
@Builder
@Schema(description = "Результат анализа схемы")
public class SchemaCheckResponse {

    @Schema(description = "Флаг совместимости", example = "true")
    private boolean compatible;
    private String mode;
    private List<Issue> issues;
    private DiffResult diff;

    @Schema(description = "Числовая оценка риска", example = "65")
    private int riskScore;
    @Schema(description = "Уровень риска", example = "MEDIUM")
    private String riskLevel;
    @Schema(description = "Governance-решение", example = "REQUIRE_CONSUMER_UPGRADE_FIRST")
    private String decision;
    @Schema(description = "Пояснение к решению")
    private List<String> decisionExplanation;
    private List<String> recommendations;
    private ImpactResponse impact;

    public static SchemaCheckResponse fromResult(
            CompatibilityResult result,
            DiffResult diffResult,
            RiskResult riskResult,
            GovernanceDecision governanceDecision,
            List<String> decisionExplanation,
            List<String> recommendations,
            ImpactResult impactResult
    ) {
        return SchemaCheckResponse.builder()
                .compatible(result.isCompatible())
                .mode(result.getMode().name())
                .issues(Objects.requireNonNullElse(result.getIssues(), List.of()))
                .diff(diffResult)
                .riskScore(riskResult.getRiskScore())
                .riskLevel(riskResult.getRiskLevel().name())
                .decision(governanceDecision != null ? governanceDecision.name() : riskResult.getDecision().name())
                .decisionExplanation(Objects.requireNonNullElse(decisionExplanation, List.of()))
                .recommendations(Objects.requireNonNullElse(recommendations, List.of()))
                .impact(ImpactResponse.fromResult(impactResult))
                .build();
    }

    public static SchemaCheckResponse fromResult(AnalyzeSchemaChangeResult result) {
        return fromResult(
                result.compatibilityResult(),
                result.diffResult(),
                result.riskResult(),
                result.governanceDecision(),
                result.decisionExplanation(),
                result.recommendations(),
                result.impact()
        );
    }
}
