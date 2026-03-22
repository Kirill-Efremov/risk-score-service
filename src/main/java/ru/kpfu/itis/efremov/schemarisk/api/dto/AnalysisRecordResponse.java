package ru.kpfu.itis.efremov.schemarisk.api.dto;

import ru.kpfu.itis.efremov.schemarisk.application.analysis.model.AnalysisRecord;

import java.time.Instant;
import java.util.List;

public record AnalysisRecordResponse(
        Long id,
        String subject,
        Integer oldVersion,
        Integer newVersion,
        String compatibilityMode,
        boolean formalCompatible,
        List<?> issues,
        Object diff,
        int riskScore,
        String riskLevel,
        String decision,
        List<String> recommendations,
        ImpactResponse impact,
        Instant createdAt,
        String createdBy
) {
    public static AnalysisRecordResponse fromRecord(AnalysisRecord record) {
        return new AnalysisRecordResponse(
                record.id(),
                record.subjectName(),
                record.oldVersion(),
                record.newVersion(),
                record.compatibilityMode().name(),
                record.formalCompatible(),
                record.issues(),
                record.diff(),
                record.riskScore(),
                record.riskLevel().name(),
                record.decision().name(),
                record.recommendations(),
                ImpactResponse.fromResult(record.impact()),
                record.createdAt(),
                record.createdBy()
        );
    }
}
