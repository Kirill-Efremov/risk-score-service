package ru.kpfu.itis.efremov.schemarisk.analysis.model;

import ru.kpfu.itis.efremov.schemarisk.catalog.model.SchemaVersionInfo;
import ru.kpfu.itis.efremov.schemarisk.common.model.SchemaType;

public record ResolvedVersionedSchemaChange(
        String subject,
        SchemaType schemaType,
        String oldSchema,
        String newSchema,
        SchemaVersionInfo oldSchemaVersion,
        SchemaVersionInfo newSchemaVersion
) {
}




