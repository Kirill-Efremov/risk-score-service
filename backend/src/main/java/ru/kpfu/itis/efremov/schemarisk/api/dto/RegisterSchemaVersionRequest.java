package ru.kpfu.itis.efremov.schemarisk.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.kpfu.itis.efremov.schemarisk.catalog.model.SchemaSourceType;
import ru.kpfu.itis.efremov.schemarisk.catalog.model.SchemaVersionStatus;
import ru.kpfu.itis.efremov.schemarisk.common.model.CompatibilityMode;
import ru.kpfu.itis.efremov.schemarisk.common.model.SchemaType;

@Data
public class RegisterSchemaVersionRequest {

    @NotNull(message = "schemaType is required")
    private SchemaType schemaType;

    private CompatibilityMode defaultCompatibilityMode;

    private String description;

    @NotBlank(message = "schemaText must not be blank")
    private String schemaText;

    private SchemaVersionStatus status;

    private SchemaSourceType sourceType;

    private String externalSchemaId;
}




