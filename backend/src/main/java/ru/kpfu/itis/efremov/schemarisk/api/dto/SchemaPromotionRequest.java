package ru.kpfu.itis.efremov.schemarisk.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.kpfu.itis.efremov.schemarisk.common.model.CompatibilityMode;
import ru.kpfu.itis.efremov.schemarisk.common.model.SchemaType;

@Data
@Schema(description = "Controlled schema promotion request")
public class SchemaPromotionRequest {

    @NotNull(message = "schemaType is required")
    @Schema(
            description = "Schema type: AVRO, JSON_SCHEMA, PROTOBUF. JSON_SCHEMA and PROTOBUF use enhanced project-level compatibility and risk analysis.",
            example = "AVRO"
    )
    private SchemaType schemaType;

    @Schema(
            description = "Compatibility mode for analysis. If omitted, the subject default policy is used.",
            example = "BACKWARD"
    )
    private CompatibilityMode compatibilityMode;

    @NotBlank(message = "schemaText must not be blank")
    @Schema(description = "Candidate schema text to analyze and potentially register")
    private String schemaText;

    @Schema(description = "Optional schema version description")
    private String description;

    @Schema(description = "Legacy initiator field kept for backward compatibility. Backend resolves current user from JWT.")
    private String createdBy;
}
