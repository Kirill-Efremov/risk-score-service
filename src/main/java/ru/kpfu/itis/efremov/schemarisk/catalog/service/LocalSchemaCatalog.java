package ru.kpfu.itis.efremov.schemarisk.catalog.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.kpfu.itis.efremov.schemarisk.catalog.model.RegisterSchemaVersionCommand;
import ru.kpfu.itis.efremov.schemarisk.catalog.model.SchemaSourceType;
import ru.kpfu.itis.efremov.schemarisk.catalog.model.SchemaVersionInfo;
import ru.kpfu.itis.efremov.schemarisk.catalog.model.SchemaVersionStatus;
import ru.kpfu.itis.efremov.schemarisk.common.port.SchemaCatalog;
import ru.kpfu.itis.efremov.schemarisk.catalog.persistence.entity.SchemaSubjectEntity;
import ru.kpfu.itis.efremov.schemarisk.catalog.persistence.entity.SchemaVersionEntity;
import ru.kpfu.itis.efremov.schemarisk.catalog.persistence.repository.SchemaSubjectRepository;
import ru.kpfu.itis.efremov.schemarisk.catalog.persistence.repository.SchemaVersionRepository;
import ru.kpfu.itis.efremov.schemarisk.common.model.CompatibilityMode;
import ru.kpfu.itis.efremov.schemarisk.common.exception.InvalidRequestException;
import ru.kpfu.itis.efremov.schemarisk.common.exception.ResourceNotFoundException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "schema-catalog", name = "mode", havingValue = "local", matchIfMissing = true)
public class LocalSchemaCatalog implements SchemaCatalog {

    private final SchemaSubjectRepository schemaSubjectRepository;
    private final SchemaVersionRepository schemaVersionRepository;
    private final SchemaCatalogMapper schemaCatalogMapper;

    public LocalSchemaCatalog(
            SchemaSubjectRepository schemaSubjectRepository,
            SchemaVersionRepository schemaVersionRepository,
            SchemaCatalogMapper schemaCatalogMapper
    ) {
        this.schemaSubjectRepository = schemaSubjectRepository;
        this.schemaVersionRepository = schemaVersionRepository;
        this.schemaCatalogMapper = schemaCatalogMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public SchemaVersionInfo getLatestVersion(String subject) {
        SchemaVersionEntity entity = schemaVersionRepository.findTopBySubject_NameOrderByVersionDesc(subject)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found: " + subject));
        return schemaCatalogMapper.toVersionInfo(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public SchemaVersionInfo getVersion(String subject, int version) {
        SchemaVersionEntity entity = schemaVersionRepository.findBySubject_NameAndVersion(subject, version)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Schema version not found: subject=" + subject + ", version=" + version
                ));
        return schemaCatalogMapper.toVersionInfo(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchemaVersionInfo> listVersions(String subject) {
        List<SchemaVersionEntity> entities = schemaVersionRepository.findAllBySubject_NameOrderByVersionDesc(subject);
        if (entities.isEmpty()) {
            throw new ResourceNotFoundException("Subject not found: " + subject);
        }
        return entities.stream()
                .map(schemaCatalogMapper::toVersionInfo)
                .toList();
    }

    @Override
    @Transactional
    public SchemaVersionInfo registerSchemaVersion(RegisterSchemaVersionCommand command) {
        SchemaSubjectEntity subject = schemaSubjectRepository.findByNameForUpdate(command.subject())
                .orElseGet(() -> createSubject(command));

        validateSubjectConsistency(subject, command);

        SchemaVersionEntity versionEntity = new SchemaVersionEntity();
        versionEntity.setSubject(subject);
        versionEntity.setVersion(nextVersion(subject.getId()));
        versionEntity.setSchemaText(command.schemaText());
        versionEntity.setSchemaHash(calculateHash(command.schemaText()));
        versionEntity.setStatus(resolveStatus(command.status()));
        versionEntity.setSourceType(resolveSourceType(command.sourceType()));
        versionEntity.setExternalSchemaId(command.externalSchemaId());
        versionEntity.setCreatedAt(Instant.now());

        SchemaVersionEntity savedEntity = schemaVersionRepository.save(versionEntity);
        return schemaCatalogMapper.toVersionInfo(savedEntity);
    }

    private SchemaSubjectEntity createSubject(RegisterSchemaVersionCommand command) {
        SchemaSubjectEntity subject = new SchemaSubjectEntity();
        subject.setName(command.subject());
        subject.setSchemaType(command.schemaType());
        subject.setDefaultCompatibilityMode(resolveCompatibilityMode(command.defaultCompatibilityMode()));
        subject.setDescription(command.description());
        subject.setCreatedAt(Instant.now());
        return schemaSubjectRepository.save(subject);
    }

    private void validateSubjectConsistency(SchemaSubjectEntity subject, RegisterSchemaVersionCommand command) {
        if (subject.getSchemaType() != command.schemaType()) {
            throw new InvalidRequestException("Subject schema type mismatch for subject: " + command.subject());
        }

        CompatibilityMode requestedMode = resolveCompatibilityMode(command.defaultCompatibilityMode());
        if (subject.getDefaultCompatibilityMode() != requestedMode) {
            throw new InvalidRequestException(
                    "Subject compatibility mode mismatch for subject: " + command.subject()
            );
        }
    }

    private int nextVersion(Long subjectId) {
        Integer currentMax = schemaVersionRepository.findMaxVersionBySubjectId(subjectId);
        return currentMax == null ? 1 : currentMax + 1;
    }

    private CompatibilityMode resolveCompatibilityMode(CompatibilityMode compatibilityMode) {
        return compatibilityMode != null ? compatibilityMode : CompatibilityMode.BACKWARD;
    }

    private SchemaVersionStatus resolveStatus(SchemaVersionStatus status) {
        return status != null ? status : SchemaVersionStatus.ACTIVE;
    }

    private SchemaSourceType resolveSourceType(SchemaSourceType sourceType) {
        return sourceType != null ? sourceType : SchemaSourceType.LOCAL;
    }

    private String calculateHash(String schemaText) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hash = messageDigest.digest(schemaText.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is not available", exception);
        }
    }
}
