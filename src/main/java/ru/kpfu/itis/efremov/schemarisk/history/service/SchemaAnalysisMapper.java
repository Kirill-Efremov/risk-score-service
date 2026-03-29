package ru.kpfu.itis.efremov.schemarisk.history.service;

import org.springframework.stereotype.Component;
import ru.kpfu.itis.efremov.schemarisk.history.model.AnalysisRecord;
import ru.kpfu.itis.efremov.schemarisk.history.persistence.entity.SchemaAnalysisEntity;

@Component
public class SchemaAnalysisMapper {

    private final AnalysisJsonMapper analysisJsonMapper;

    public SchemaAnalysisMapper(AnalysisJsonMapper analysisJsonMapper) {
        this.analysisJsonMapper = analysisJsonMapper;
    }

    public AnalysisRecord toRecord(SchemaAnalysisEntity entity) {
        return new AnalysisRecord(
                entity.getId(),
                entity.getSubject() != null ? entity.getSubject().getId() : null,
                entity.getSubjectName() != null ? entity.getSubjectName() : entity.getSubject() != null ? entity.getSubject().getName() : null,
                entity.getOldVersion() != null ? entity.getOldVersion().getId() : null,
                entity.getOldVersionNumber() != null ? entity.getOldVersionNumber() : entity.getOldVersion() != null ? entity.getOldVersion().getVersion() : null,
                entity.getNewVersion() != null ? entity.getNewVersion().getId() : null,
                entity.getNewVersionNumber() != null ? entity.getNewVersionNumber() : entity.getNewVersion() != null ? entity.getNewVersion().getVersion() : null,
                entity.getSourceType(),
                entity.getExternalSchemaId(),
                entity.getCompatibilityMode(),
                entity.isFormalCompatible(),
                analysisJsonMapper.readIssues(entity.getIssuesJson()),
                analysisJsonMapper.readDiff(entity.getDiffJson()),
                entity.getRiskScore(),
                entity.getRiskLevel(),
                entity.getDecision(),
                analysisJsonMapper.readRecommendations(entity.getRecommendationsJson()),
                analysisJsonMapper.readImpact(entity.getImpactJson()),
                entity.getCreatedAt(),
                entity.getCreatedBy()
        );
    }
}
