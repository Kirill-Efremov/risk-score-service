package ru.kpfu.itis.efremov.schemarisk.api.dto;

import ru.kpfu.itis.efremov.schemarisk.catalog.model.SchemaSubjectInfo;

import java.time.Instant;

public record SchemaSubjectResponse(
        String name,
        String schemaType,
        String defaultCompatibilityMode,
        String description,
        Instant createdAt
) {
    public static SchemaSubjectResponse fromInfo(SchemaSubjectInfo info) {
        return new SchemaSubjectResponse(
                info.name(),
                info.schemaType().name(),
                info.defaultCompatibilityMode().name(),
                info.description(),
                info.createdAt()
        );
    }
}




