package ru.kpfu.itis.efremov.schemarisk.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import ru.kpfu.itis.efremov.schemarisk.application.usage.model.ServiceRole;

@Data
public class RegisterServiceUsageRequest {

    @NotBlank(message = "subject must not be blank")
    private String subject;

    @Positive(message = "version must be positive")
    private Integer version;

    @NotNull(message = "role is required")
    private ServiceRole role;

    private Boolean active;
}
