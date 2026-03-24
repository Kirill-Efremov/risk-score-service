package ru.kpfu.itis.efremov.schemarisk.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import ru.kpfu.itis.efremov.schemarisk.usage.model.ServiceRole;

@Data
@Schema(description = "Запрос на регистрацию использования схемы сервисом")
public class RegisterServiceUsageRequest {

    @NotBlank(message = "subject must not be blank")
    @Schema(description = "Имя subject", example = "user-created")
    private String subject;

    @Positive(message = "version must be positive")
    @Schema(description = "Версия схемы", example = "2")
    private Integer version;

    @NotNull(message = "role is required")
    @Schema(description = "Роль сервиса относительно схемы", example = "CONSUMER")
    private ServiceRole role;

    @Schema(description = "Флаг активности usage", example = "true")
    private Boolean active;
}




