package ru.kpfu.itis.efremov.schemarisk.history.model;

import ru.kpfu.itis.efremov.schemarisk.analysis.impact.ImpactResult;
import ru.kpfu.itis.efremov.schemarisk.analysis.diff.DiffResult;
import ru.kpfu.itis.efremov.schemarisk.analysis.compatibility.CompatibilityResult;
import ru.kpfu.itis.efremov.schemarisk.analysis.governance.GovernanceDecision;
import ru.kpfu.itis.efremov.schemarisk.analysis.governance.StructuredRecommendation;
import ru.kpfu.itis.efremov.schemarisk.analysis.risk.RiskResult;
import ru.kpfu.itis.efremov.schemarisk.catalog.model.SchemaSourceType;

import java.util.List;

public record SaveAnalysisCommand(
        Long subjectId,
        String subjectName,
        Long oldVersionId,
        Integer oldVersion,
        Long newVersionId,
        Integer newVersion,
        SchemaSourceType sourceType,
        String externalSchemaId,
        CompatibilityResult compatibilityResult,
        DiffResult diffResult,
        RiskResult riskResult,
        GovernanceDecision governanceDecision,
        List<String> decisionExplanation,
        List<String> recommendations,
        List<StructuredRecommendation> structuredRecommendations,
        ImpactResult impact,
        String createdBy
) {
}
