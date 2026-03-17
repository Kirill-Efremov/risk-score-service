package ru.kpfu.itis.efremov.schemarisk.application.catalog.model;

import ru.kpfu.itis.efremov.schemarisk.model.CompatibilityMode;
import ru.kpfu.itis.efremov.schemarisk.model.SchemaType;

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
