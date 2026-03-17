package ru.kpfu.itis.efremov.schemarisk.application.analysis.model;

import ru.kpfu.itis.efremov.schemarisk.core.diff.DiffResult;
import ru.kpfu.itis.efremov.schemarisk.core.engine.CompatibilityResult;
import ru.kpfu.itis.efremov.schemarisk.core.risk.RiskResult;

import java.util.List;

public record SaveAnalysisCommand(
        Long subjectId,
        String subjectName,
        Long oldVersionId,
        Integer oldVersion,
        Long newVersionId,
        Integer newVersion,
        CompatibilityResult compatibilityResult,
        DiffResult diffResult,
        RiskResult riskResult,
        List<String> recommendations,
        String createdBy
) {
}
