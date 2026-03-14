package ru.kpfu.itis.efremov.schemarisk.schema;

public record SchemaReference(
        String name,
        String subject,
        Integer version
) {
}

