package ru.kpfu.itis.efremov.schemarisk.analysis.diff;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class NormalizedSchema {

    private final String name;
    private final Map<String, NormalizedField> fields;

    public NormalizedSchema(String name, Map<String, NormalizedField> fields) {
        this.name = name;
        this.fields = fields == null ? Map.of() : Map.copyOf(new LinkedHashMap<>(fields));
    }

    public String getName() {
        return name;
    }

    public Map<String, NormalizedField> getFields() {
        return Objects.requireNonNullElse(fields, Map.of());
    }
}




