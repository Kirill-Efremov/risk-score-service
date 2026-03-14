package ru.kpfu.itis.efremov.schemarisk.application.catalog.model;

import ru.kpfu.itis.efremov.schemarisk.model.CompatibilityMode;
import ru.kpfu.itis.efremov.schemarisk.model.SchemaType;

public record RegisterSchemaVersionCommand(
        String subject,
        SchemaType schemaType,
        CompatibilityMode defaultCompatibilityMode,
        String description,
        String schemaText,
        SchemaVersionStatus status,
        SchemaSourceType sourceType,
        String externalSchemaId
) {
}
