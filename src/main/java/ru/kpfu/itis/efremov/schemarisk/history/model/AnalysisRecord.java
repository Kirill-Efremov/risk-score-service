package ru.kpfu.itis.efremov.schemarisk.history.model;

import ru.kpfu.itis.efremov.schemarisk.analysis.impact.ImpactResult;
import ru.kpfu.itis.efremov.schemarisk.analysis.diff.DiffResult;
import ru.kpfu.itis.efremov.schemarisk.catalog.model.SchemaSourceType;
import ru.kpfu.itis.efremov.schemarisk.common.model.CompatibilityMode;
import ru.kpfu.itis.efremov.schemarisk.common.model.Decision;
import ru.kpfu.itis.efremov.schemarisk.common.model.Issue;
import ru.kpfu.itis.efremov.schemarisk.analysis.risk.RiskLevel;

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
        SchemaSourceType sourceType,
        String externalSchemaId,
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
