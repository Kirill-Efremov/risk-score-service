package ru.kpfu.itis.efremov.schemarisk.application.usecase;

import ru.kpfu.itis.efremov.schemarisk.application.impact.model.ImpactResult;
import ru.kpfu.itis.efremov.schemarisk.application.recommendation.model.GovernanceDecision;
import ru.kpfu.itis.efremov.schemarisk.core.diff.DiffResult;
import ru.kpfu.itis.efremov.schemarisk.core.engine.CompatibilityResult;
import ru.kpfu.itis.efremov.schemarisk.core.risk.RiskResult;

import java.util.List;

public record AnalyzeSchemaChangeResult(
        CompatibilityResult compatibilityResult,
        DiffResult diffResult,
        RiskResult riskResult,
        List<String> recommendations,
        ImpactResult impact,
        GovernanceDecision governanceDecision,
        List<String> decisionExplanation
) {
}
