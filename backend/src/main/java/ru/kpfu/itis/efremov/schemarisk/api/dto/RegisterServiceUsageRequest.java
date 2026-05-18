package ru.kpfu.itis.efremov.schemarisk.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import ru.kpfu.itis.efremov.schemarisk.usage.model.ServiceRole;

@Data
@Schema(description = "Request for service usage registration")
public class RegisterServiceUsageRequest {

    @NotBlank(message = "subject must not be blank")
    @Schema(description = "Subject name", example = "user-created")
    private String subject;

    @NotNull(message = "version is required")
    @Positive(message = "version must be positive")
    @Schema(description = "Schema version", example = "2")
    private Integer version;

    @NotNull(message = "role is required")
    @Schema(description = "Service role relative to the schema", example = "CONSUMER")
    private ServiceRole role;

    @Schema(description = "Whether the usage is active", example = "true")
    private Boolean active;

    @Schema(description = "Who created the usage link", example = "system")
    private String createdBy;
}
