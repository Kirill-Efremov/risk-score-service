package ru.kpfu.itis.efremov.schemarisk.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request for partial service update")
public class UpdateServiceRequest {

    @Schema(description = "New service name", example = "billing-service")
    private String name;

    @Schema(description = "Whether the service is critical", example = "true")
    private Boolean critical;

    @Schema(description = "Whether the service is active", example = "true")
    private Boolean active;

    @Schema(description = "Service owner", example = "billing-team")
    private String owner;

    @Schema(description = "Service description", example = "Billing service")
    private String description;

    @Schema(description = "Who changed the service", example = "system")
    private String changedBy;
}
