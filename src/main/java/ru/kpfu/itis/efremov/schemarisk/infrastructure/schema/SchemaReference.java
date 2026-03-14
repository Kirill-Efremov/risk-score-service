package ru.kpfu.itis.efremov.schemarisk.infrastructure.schema;

public record SchemaReference(
        String name,
        String subject,
        Integer version
) {
}
