package ru.kpfu.itis.efremov.schemarisk.api.dto;

import lombok.Builder;
import lombok.Data;
import ru.kpfu.itis.efremov.schemarisk.core.diff.DiffResult;
import ru.kpfu.itis.efremov.schemarisk.core.engine.CompatibilityResult;
import ru.kpfu.itis.efremov.schemarisk.core.risk.RiskResult;
import ru.kpfu.itis.efremov.schemarisk.model.Issue;

import java.util.List;
import java.util.Objects;

@Data
@Builder
public class SchemaCheckResponse {

    private boolean compatible;
    private String mode;
    private List<Issue> issues;
    private DiffResult diff;

    private int riskScore;
    private String riskLevel;
    private String decision;
    private List<String> recommendations;

    public static SchemaCheckResponse fromResult(
            CompatibilityResult result,
            DiffResult diffResult,
            RiskResult riskResult,
            List<String> recommendations
    ) {
        return SchemaCheckResponse.builder()
                .compatible(result.isCompatible())
                .mode(result.getMode().name())
                .issues(Objects.requireNonNullElse(result.getIssues(), List.of()))
                .diff(diffResult)
                .riskScore(riskResult.getRiskScore())
                .riskLevel(riskResult.getRiskLevel().name())
                .decision(riskResult.getDecision().name())
                .recommendations(Objects.requireNonNullElse(recommendations, List.of()))
                .build();
    }
}
