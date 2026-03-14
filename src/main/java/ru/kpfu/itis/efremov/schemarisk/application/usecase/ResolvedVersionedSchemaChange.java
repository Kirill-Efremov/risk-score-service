package ru.kpfu.itis.efremov.schemarisk.application.usecase;

import ru.kpfu.itis.efremov.schemarisk.application.catalog.model.SchemaVersionInfo;
import ru.kpfu.itis.efremov.schemarisk.model.SchemaType;

public record ResolvedVersionedSchemaChange(
        SchemaType schemaType,
        String oldSchema,
        String newSchema,
        SchemaVersionInfo oldSchemaVersion,
        SchemaVersionInfo newSchemaVersion
) {
}
