package ru.kpfu.itis.efremov.schemarisk.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import ru.kpfu.itis.efremov.schemarisk.usage.model.ServiceRole;

@Data
@Schema(description = "Request for partial usage update")
public class UpdateServiceUsageRequest {

    @Schema(description = "Subject name", example = "user-created")
    private String subject;

    @Positive(message = "version must be positive")
    @Schema(description = "Schema version", example = "3")
    private Integer version;

    @Schema(description = "Service role relative to the schema", example = "CONSUMER")
    private ServiceRole role;

    @Schema(description = "Whether the usage is active", example = "true")
    private Boolean active;

    @Schema(description = "Who changed the usage link", example = "system")
    private String changedBy;
}
