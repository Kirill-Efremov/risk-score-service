package ru.kpfu.itis.efremov.schemarisk.application.usecase;

import ru.kpfu.itis.efremov.schemarisk.model.CompatibilityMode;
import ru.kpfu.itis.efremov.schemarisk.model.SchemaType;

public record AnalyzeVersionedSchemaChangeCommand(
        String subject,
        Integer oldVersion,
        Integer newVersion,
        String newSchema,
        SchemaType schemaType,
        CompatibilityMode compatibilityMode
) {
}
