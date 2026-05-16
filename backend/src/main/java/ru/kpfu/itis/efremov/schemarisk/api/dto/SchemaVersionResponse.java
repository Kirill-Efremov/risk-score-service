package ru.kpfu.itis.efremov.schemarisk.api.dto;

import ru.kpfu.itis.efremov.schemarisk.catalog.model.SchemaVersionInfo;

import java.time.Instant;

public record SchemaVersionResponse(
        SchemaSubjectResponse subject,
        int version,
        String schemaText,
        String schemaHash,
        String status,
        String sourceType,
        String externalSchemaId,
        Instant createdAt
) {
    public static SchemaVersionResponse fromInfo(SchemaVersionInfo info) {
        return new SchemaVersionResponse(
                SchemaSubjectResponse.fromInfo(info.subject()),
                info.version(),
                info.schemaText(),
                info.schemaHash(),
                info.status().name(),
                info.sourceType().name(),
                info.externalSchemaId(),
                info.createdAt()
        );
    }
}




