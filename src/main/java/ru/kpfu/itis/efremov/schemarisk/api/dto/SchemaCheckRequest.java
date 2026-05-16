package ru.kpfu.itis.efremov.schemarisk.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.kpfu.itis.efremov.schemarisk.common.model.CompatibilityMode;
import ru.kpfu.itis.efremov.schemarisk.common.model.SchemaType;

@Data
@Schema(description = "Request for standalone raw schema analysis without publication")
public class SchemaCheckRequest {

    @NotNull(message = "schemaType is required")
    @Schema(description = "Schema type", example = "AVRO")
    private SchemaType schemaType;

    @Schema(description = "Compatibility mode", example = "BACKWARD")
    private CompatibilityMode compatibilityMode;

    @NotBlank(message = "oldSchema must not be blank")
    @Schema(description = "Previous schema text")
    private String oldSchema;

    @NotBlank(message = "newSchema must not be blank")
    @Schema(description = "Candidate schema text")
    private String newSchema;
}
