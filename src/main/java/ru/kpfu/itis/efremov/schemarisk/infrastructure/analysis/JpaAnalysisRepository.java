package ru.kpfu.itis.efremov.schemarisk.infrastructure.analysis;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.kpfu.itis.efremov.schemarisk.application.analysis.model.AnalysisRecord;
import ru.kpfu.itis.efremov.schemarisk.application.analysis.model.SaveAnalysisCommand;
import ru.kpfu.itis.efremov.schemarisk.application.port.AnalysisRepository;
import ru.kpfu.itis.efremov.schemarisk.infrastructure.analysis.persistence.entity.SchemaAnalysisEntity;
import ru.kpfu.itis.efremov.schemarisk.infrastructure.analysis.persistence.repository.SchemaAnalysisRepository;
import ru.kpfu.itis.efremov.schemarisk.infrastructure.catalog.persistence.entity.SchemaSubjectEntity;
import ru.kpfu.itis.efremov.schemarisk.infrastructure.catalog.persistence.entity.SchemaVersionEntity;
import ru.kpfu.itis.efremov.schemarisk.infrastructure.catalog.persistence.repository.SchemaSubjectRepository;
import ru.kpfu.itis.efremov.schemarisk.infrastructure.catalog.persistence.repository.SchemaVersionRepository;
import ru.kpfu.itis.efremov.schemarisk.support.exception.ResourceNotFoundException;

import java.time.Instant;
import java.util.List;

@Component
public class JpaAnalysisRepository implements AnalysisRepository {

    private final SchemaAnalysisRepository schemaAnalysisRepository;
    private final SchemaSubjectRepository schemaSubjectRepository;
    private final SchemaVersionRepository schemaVersionRepository;
    private final AnalysisJsonMapper analysisJsonMapper;
    private final SchemaAnalysisMapper schemaAnalysisMapper;

    public JpaAnalysisRepository(
            SchemaAnalysisRepository schemaAnalysisRepository,
            SchemaSubjectRepository schemaSubjectRepository,
            SchemaVersionRepository schemaVersionRepository,
            AnalysisJsonMapper analysisJsonMapper,
            SchemaAnalysisMapper schemaAnalysisMapper
    ) {
        this.schemaAnalysisRepository = schemaAnalysisRepository;
        this.schemaSubjectRepository = schemaSubjectRepository;
        this.schemaVersionRepository = schemaVersionRepository;
        this.analysisJsonMapper = analysisJsonMapper;
        this.schemaAnalysisMapper = schemaAnalysisMapper;
    }

    @Override
    @Transactional
    public AnalysisRecord save(SaveAnalysisCommand command) {
        SchemaAnalysisEntity entity = new SchemaAnalysisEntity();
        entity.setSubject(resolveSubject(command.subjectId()));
        entity.setOldVersion(resolveVersion(command.oldVersionId()));
        entity.setNewVersion(resolveVersion(command.newVersionId()));
        entity.setCompatibilityMode(command.compatibilityResult().getMode());
        entity.setFormalCompatible(command.compatibilityResult().isCompatible());
        entity.setIssuesJson(analysisJsonMapper.writeIssues(command.compatibilityResult().getIssues()));
        entity.setDiffJson(analysisJsonMapper.writeDiff(command.diffResult()));
        entity.setRiskScore(command.riskResult().getRiskScore());
        entity.setRiskLevel(command.riskResult().getRiskLevel());
        entity.setDecision(command.riskResult().getDecision());
        entity.setRecommendationsJson(analysisJsonMapper.writeRecommendations(command.recommendations()));
        entity.setImpactJson(analysisJsonMapper.writeImpact(command.impact()));
        entity.setCreatedAt(Instant.now());
        entity.setCreatedBy(command.createdBy());

        return schemaAnalysisMapper.toRecord(schemaAnalysisRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public AnalysisRecord getById(Long analysisId) {
        SchemaAnalysisEntity entity = schemaAnalysisRepository.findById(analysisId)
                .orElseThrow(() -> new ResourceNotFoundException("Analysis not found: " + analysisId));
        return schemaAnalysisMapper.toRecord(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnalysisRecord> listBySubject(String subject) {
        if (schemaSubjectRepository.findByName(subject).isEmpty()) {
            throw new ResourceNotFoundException("Subject not found: " + subject);
        }
        return schemaAnalysisRepository.findAllBySubject_NameOrderByCreatedAtDesc(subject).stream()
                .map(schemaAnalysisMapper::toRecord)
                .toList();
    }

    private SchemaSubjectEntity resolveSubject(Long subjectId) {
        if (subjectId == null) {
            return null;
        }
        return schemaSubjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found by id: " + subjectId));
    }

    private SchemaVersionEntity resolveVersion(Long versionId) {
        if (versionId == null) {
            return null;
        }
        return schemaVersionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Schema version not found by id: " + versionId));
    }
}
