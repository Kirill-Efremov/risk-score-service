package ru.kpfu.itis.efremov.schemarisk.application.usecase;

import ru.kpfu.itis.efremov.schemarisk.model.CompatibilityMode;
import ru.kpfu.itis.efremov.schemarisk.model.SchemaType;

public record AnalyzeSchemaChangeCommand(
        SchemaType schemaType,
        CompatibilityMode compatibilityMode,
        String oldSchema,
        String newSchema
) {
}
