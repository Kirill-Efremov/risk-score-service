package ru.kpfu.itis.efremov.schemarisk.catalog.model;

import ru.kpfu.itis.efremov.schemarisk.common.model.CompatibilityMode;
import ru.kpfu.itis.efremov.schemarisk.common.model.SchemaType;

import java.time.Instant;

public record SchemaSubjectInfo(
        Long id,
        String name,
        SchemaType schemaType,
        CompatibilityMode defaultCompatibilityMode,
        String description,
        Instant createdAt
) {
}




