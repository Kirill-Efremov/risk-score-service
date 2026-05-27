package ru.kpfu.itis.efremov.schemarisk.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Supported schema type: AVRO, JSON_SCHEMA, PROTOBUF")
public enum SchemaType {
    AVRO,
    JSON_SCHEMA,
    PROTOBUF;

    @JsonCreator
    public static SchemaType fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return switch (value.trim().toUpperCase()) {
            case "AVRO" -> AVRO;
            case "JSON", "JSON_SCHEMA" -> JSON_SCHEMA;
            case "PROTO", "PROTOBUF" -> PROTOBUF;
            default -> throw new IllegalArgumentException("Unsupported schema type: " + value);
        };
    }

    @JsonValue
    public String jsonValue() {
        return name();
    }
}
