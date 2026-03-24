package ru.kpfu.itis.efremov.schemarisk.history.model;

import ru.kpfu.itis.efremov.schemarisk.analysis.impact.ImpactResult;
import ru.kpfu.itis.efremov.schemarisk.analysis.diff.DiffResult;
import ru.kpfu.itis.efremov.schemarisk.analysis.compatibility.CompatibilityResult;
import ru.kpfu.itis.efremov.schemarisk.analysis.risk.RiskResult;

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
        ImpactResult impact,
        String createdBy
) {
}




