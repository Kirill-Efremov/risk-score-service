package ru.kpfu.itis.efremov.schemarisk.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request for service creation")
public class CreateServiceRequest {

    @NotBlank(message = "name must not be blank")
    @Schema(description = "Unique service name", example = "billing-service")
    private String name;

    @Schema(description = "Whether the service is critical", example = "true")
    private boolean critical;

    @Schema(description = "Service owner", example = "billing-team")
    private String owner;

    @Schema(description = "Service description", example = "Billing service")
    private String description;

    @Schema(description = "Who created the service", example = "system")
    private String createdBy;
}
