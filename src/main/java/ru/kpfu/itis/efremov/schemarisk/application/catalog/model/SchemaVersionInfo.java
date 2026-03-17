package ru.kpfu.itis.efremov.schemarisk.application.catalog.model;

import java.time.Instant;

public record SchemaVersionInfo(
        Long id,
        SchemaSubjectInfo subject,
        int version,
        String schemaText,
        String schemaHash,
        SchemaVersionStatus status,
        SchemaSourceType sourceType,
        String externalSchemaId,
        Instant createdAt
) {
}
