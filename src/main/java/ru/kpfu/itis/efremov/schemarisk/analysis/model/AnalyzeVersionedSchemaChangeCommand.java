package ru.kpfu.itis.efremov.schemarisk.analysis.model;

import ru.kpfu.itis.efremov.schemarisk.common.model.CompatibilityMode;
import ru.kpfu.itis.efremov.schemarisk.common.model.SchemaType;

public record AnalyzeVersionedSchemaChangeCommand(
        String subject,
        Integer oldVersion,
        Integer newVersion,
        String newSchema,
        SchemaType schemaType,
        CompatibilityMode compatibilityMode
) {
}




