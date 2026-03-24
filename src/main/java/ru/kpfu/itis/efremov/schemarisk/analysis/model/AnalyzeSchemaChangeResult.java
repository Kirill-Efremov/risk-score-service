package ru.kpfu.itis.efremov.schemarisk.analysis.model;

import ru.kpfu.itis.efremov.schemarisk.analysis.impact.ImpactResult;
import ru.kpfu.itis.efremov.schemarisk.analysis.governance.GovernanceDecision;
import ru.kpfu.itis.efremov.schemarisk.analysis.diff.DiffResult;
import ru.kpfu.itis.efremov.schemarisk.analysis.compatibility.CompatibilityResult;
import ru.kpfu.itis.efremov.schemarisk.analysis.risk.RiskResult;

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




