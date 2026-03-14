package ru.kpfu.itis.efremov.schemarisk.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.kpfu.itis.efremov.schemarisk.model.CompatibilityMode;
import ru.kpfu.itis.efremov.schemarisk.model.SchemaType;

@Data
public class SchemaCheckRequest {

    @NotNull(message = "schemaType is required")
    private SchemaType schemaType;

    private CompatibilityMode compatibilityMode;

    @NotBlank(message = "oldSchema must not be blank")
    private String oldSchema;

    @NotBlank(message = "newSchema must not be blank")
    private String newSchema;
}
