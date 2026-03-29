package ru.kpfu.itis.efremov.schemarisk.catalog.model;

import ru.kpfu.itis.efremov.schemarisk.common.model.SchemaType;

import java.time.Instant;

public record SchemaVersionInfo(
        SchemaSubjectInfo subject,
        int version,
        String schemaText,
        String schemaHash,
        SchemaType schemaType,
        SchemaVersionStatus status,
        SchemaSourceType sourceType,
        Long localId,
        String externalSchemaId,
        Instant createdAt
) {
}
