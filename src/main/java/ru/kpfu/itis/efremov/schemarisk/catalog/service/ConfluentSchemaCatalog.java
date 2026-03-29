package ru.kpfu.itis.efremov.schemarisk.catalog.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import ru.kpfu.itis.efremov.schemarisk.catalog.model.RegisterSchemaVersionCommand;
import ru.kpfu.itis.efremov.schemarisk.catalog.model.SchemaSourceType;
import ru.kpfu.itis.efremov.schemarisk.catalog.model.SchemaSubjectInfo;
import ru.kpfu.itis.efremov.schemarisk.catalog.model.SchemaVersionInfo;
import ru.kpfu.itis.efremov.schemarisk.catalog.model.SchemaVersionStatus;
import ru.kpfu.itis.efremov.schemarisk.common.exception.InvalidRequestException;
import ru.kpfu.itis.efremov.schemarisk.common.model.CompatibilityMode;
import ru.kpfu.itis.efremov.schemarisk.common.model.SchemaType;
import ru.kpfu.itis.efremov.schemarisk.common.port.SchemaCatalog;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "schema-catalog", name = "mode", havingValue = "confluent")
public class ConfluentSchemaCatalog implements SchemaCatalog {

    private final ConfluentSchemaRegistryClient client;

    public ConfluentSchemaCatalog(ConfluentSchemaRegistryClient client) {
        this.client = client;
    }

    @Override
    public SchemaVersionInfo getLatestVersion(String subject) {
        var version = client.getLatestVersion(subject);
        return toVersionInfo(version, resolveSubjectInfo(subject, version.id()));
    }

    @Override
    public SchemaVersionInfo getVersion(String subject, int version) {
        var versionResponse = client.getVersion(subject, version);
        return toVersionInfo(versionResponse, resolveSubjectInfo(subject, versionResponse.id()));
    }

    @Override
    public List<SchemaVersionInfo> listVersions(String subject) {
        SchemaSubjectInfo subjectInfo = resolveSubjectInfo(subject, null);
        return client.listVersions(subject).stream()
                .map(version -> client.getVersion(subject, version))
                .map(version -> toVersionInfo(version, subjectInfo))
                .toList();
    }

    @Override
    public SchemaVersionInfo registerSchemaVersion(RegisterSchemaVersionCommand command) {
        if (command.status() != SchemaVersionStatus.DRAFT) {
            throw new InvalidRequestException(
                    "Publishing to Confluent Schema Registry is not supported in read-first mode"
            );
        }

        SchemaVersionInfo latestVersion = getLatestVersion(command.subject());
        SchemaType schemaType = command.schemaType() != null ? command.schemaType() : latestVersion.schemaType();
        if (schemaType != latestVersion.schemaType()) {
            throw new InvalidRequestException("Provided schemaType does not match Confluent subject schema type");
        }

        SchemaSubjectInfo subjectInfo = new SchemaSubjectInfo(
                latestVersion.subject().name(),
                latestVersion.subject().schemaType(),
                latestVersion.subject().defaultCompatibilityMode(),
                command.description() != null ? command.description() : latestVersion.subject().description(),
                SchemaSourceType.CONFLUENT,
                null,
                latestVersion.subject().externalId(),
                latestVersion.subject().createdAt()
        );

        return new SchemaVersionInfo(
                subjectInfo,
                latestVersion.version() + 1,
                command.schemaText(),
                calculateHash(command.schemaText()),
                schemaType,
                SchemaVersionStatus.DRAFT,
                SchemaSourceType.CONFLUENT,
                null,
                null,
                Instant.now()
        );
    }

    private SchemaSubjectInfo resolveSubjectInfo(String subject, Integer latestSchemaId) {
        CompatibilityMode compatibilityMode = resolveCompatibilityMode(client.getSubjectConfig(subject));
        return new SchemaSubjectInfo(
                subject,
                SchemaType.AVRO,
                compatibilityMode,
                null,
                SchemaSourceType.CONFLUENT,
                null,
                latestSchemaId != null ? String.valueOf(latestSchemaId) : null,
                null
        );
    }

    private SchemaVersionInfo toVersionInfo(
            ConfluentSchemaRegistryClient.ConfluentSchemaVersionResponse response,
            SchemaSubjectInfo subjectInfo
    ) {
        return new SchemaVersionInfo(
                new SchemaSubjectInfo(
                        subjectInfo.name(),
                        subjectInfo.schemaType(),
                        subjectInfo.defaultCompatibilityMode(),
                        subjectInfo.description(),
                        SchemaSourceType.CONFLUENT,
                        null,
                        String.valueOf(response.id()),
                        subjectInfo.createdAt()
                ),
                response.version(),
                response.schema(),
                calculateHash(response.schema()),
                subjectInfo.schemaType(),
                SchemaVersionStatus.ACTIVE,
                SchemaSourceType.CONFLUENT,
                null,
                String.valueOf(response.id()),
                null
        );
    }

    private CompatibilityMode resolveCompatibilityMode(
            ConfluentSchemaRegistryClient.ConfluentSchemaConfigResponse configResponse
    ) {
        if (configResponse == null || configResponse.compatibilityLevel() == null) {
            return CompatibilityMode.BACKWARD;
        }
        String level = configResponse.compatibilityLevel().toUpperCase();
        if (level.startsWith("FULL")) {
            return CompatibilityMode.FULL;
        }
        if (level.startsWith("FORWARD")) {
            return CompatibilityMode.FORWARD;
        }
        return CompatibilityMode.BACKWARD;
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
