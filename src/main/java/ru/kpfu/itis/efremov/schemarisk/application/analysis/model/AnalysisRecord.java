package ru.kpfu.itis.efremov.schemarisk.application.analysis.model;

import ru.kpfu.itis.efremov.schemarisk.application.impact.model.ImpactResult;
import ru.kpfu.itis.efremov.schemarisk.core.diff.DiffResult;
import ru.kpfu.itis.efremov.schemarisk.model.CompatibilityMode;
import ru.kpfu.itis.efremov.schemarisk.model.Decision;
import ru.kpfu.itis.efremov.schemarisk.model.Issue;
import ru.kpfu.itis.efremov.schemarisk.core.risk.RiskLevel;

import java.time.Instant;
import java.util.List;

public record AnalysisRecord(
        Long id,
        Long subjectId,
        String subjectName,
        Long oldVersionId,
        Integer oldVersion,
        Long newVersionId,
        Integer newVersion,
        CompatibilityMode compatibilityMode,
        boolean formalCompatible,
        List<Issue> issues,
        DiffResult diff,
        int riskScore,
        RiskLevel riskLevel,
        Decision decision,
        List<String> recommendations,
        ImpactResult impact,
        Instant createdAt,
        String createdBy
) {
}
