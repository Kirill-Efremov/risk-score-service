package ru.kpfu.itis.efremov.schemarisk.analysis.model;

import ru.kpfu.itis.efremov.schemarisk.common.model.CompatibilityMode;
import ru.kpfu.itis.efremov.schemarisk.common.model.SchemaType;

public record SchemaAnalysisInput(
        SchemaType schemaType,
        CompatibilityMode compatibilityMode,
        String previousSchema,
        String candidateSchema
) {
}

