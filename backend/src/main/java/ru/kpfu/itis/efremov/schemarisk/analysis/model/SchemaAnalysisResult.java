package ru.kpfu.itis.efremov.schemarisk.analysis.model;

import ru.kpfu.itis.efremov.schemarisk.analysis.compatibility.CompatibilityResult;
import ru.kpfu.itis.efremov.schemarisk.analysis.diff.DiffResult;
import ru.kpfu.itis.efremov.schemarisk.analysis.governance.GovernanceDecision;
import ru.kpfu.itis.efremov.schemarisk.analysis.governance.StructuredRecommendation;
import ru.kpfu.itis.efremov.schemarisk.analysis.graph.dto.UsageGraphResponse;
import ru.kpfu.itis.efremov.schemarisk.analysis.impact.ImpactResult;
import ru.kpfu.itis.efremov.schemarisk.analysis.risk.RiskResult;

import java.util.List;

public record SchemaAnalysisResult(
        CompatibilityResult compatibilityResult,
        DiffResult diffResult,
        RiskResult riskResult,
        List<String> recommendations,
        List<StructuredRecommendation> structuredRecommendations,
        ImpactResult impact,
        UsageGraphResponse impactGraph,
        String oldSchemaText,
        String newSchemaText,
        GovernanceDecision governanceDecision,
        List<String> decisionExplanation,
        Long analysisId
) {
    public SchemaAnalysisResult {
        recommendations = recommendations != null ? recommendations : List.of();
        structuredRecommendations = structuredRecommendations != null ? structuredRecommendations : List.of();
        decisionExplanation = decisionExplanation != null ? decisionExplanation : List.of();
    }
}
