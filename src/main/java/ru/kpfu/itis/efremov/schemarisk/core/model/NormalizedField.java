package ru.kpfu.itis.efremov.schemarisk.core.model;

import lombok.Getter;

@Getter
public class NormalizedField {

    private final String name;
    private final String type;
    private final boolean nullable;
    private final boolean hasDefault;
    private final String defaultValue;
    private final boolean required;
    private final NormalizedSchema nestedSchema;

    private NormalizedField(
            String name,
            String type,
            boolean nullable,
            boolean hasDefault,
            String defaultValue,
            NormalizedSchema nestedSchema
    ) {
        this.name = name;
        this.type = type;
        this.nullable = nullable;
        this.hasDefault = hasDefault;
        this.defaultValue = defaultValue;
        this.required = !nullable && !hasDefault;
        this.nestedSchema = nestedSchema;
    }

    public static NormalizedField of(
            String name,
            String type,
            boolean nullable,
            boolean hasDefault,
            String defaultValue,
            NormalizedSchema nestedSchema
    ) {
        return new NormalizedField(name, type, nullable, hasDefault, defaultValue, nestedSchema);
    }

    public boolean hasDefault() {
        return hasDefault;
    }
}
