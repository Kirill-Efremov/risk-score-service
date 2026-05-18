package ru.kpfu.itis.efremov.schemarisk.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@Schema(description = "Request for usage migration to a new schema version")
public class MigrateServiceUsageRequest {

    @NotNull(message = "targetVersion is required")
    @Positive(message = "targetVersion must be positive")
    @Schema(description = "Target schema version", example = "3")
    private Integer targetVersion;

    @Schema(description = "Who initiated the migration", example = "system")
    private String changedBy;
}
