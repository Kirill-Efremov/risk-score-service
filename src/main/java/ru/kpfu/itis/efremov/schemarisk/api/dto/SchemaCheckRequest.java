package ru.kpfu.itis.efremov.schemarisk.api.dto;

import lombok.Data;
import ru.kpfu.itis.efremov.schemarisk.model.CompatibilityMode;
import ru.kpfu.itis.efremov.schemarisk.model.SchemaType;


@Data
public class SchemaCheckRequest {

    private SchemaType schemaType;              // AVRO (пока только он реально поддержан)
    private CompatibilityMode compatibilityMode; // BACKWARD / FORWARD / FULL
    private String oldSchema;
    private String newSchema;
}
