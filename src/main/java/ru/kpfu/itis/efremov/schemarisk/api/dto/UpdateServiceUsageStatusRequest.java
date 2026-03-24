package ru.kpfu.itis.efremov.schemarisk.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.kpfu.itis.efremov.schemarisk.application.usage.model.UsageStatus;

@Data
public class UpdateServiceUsageStatusRequest {

    @NotNull(message = "status is required")
    private UsageStatus status;
}
