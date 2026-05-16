package ru.kpfu.itis.efremov.schemarisk.catalog.model;

import ru.kpfu.itis.efremov.schemarisk.common.model.CompatibilityMode;
import ru.kpfu.itis.efremov.schemarisk.common.model.SchemaType;

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




