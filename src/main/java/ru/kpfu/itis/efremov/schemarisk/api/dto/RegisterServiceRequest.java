package ru.kpfu.itis.efremov.schemarisk.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Запрос на регистрацию сервиса")
public class RegisterServiceRequest {

    @NotBlank(message = "name must not be blank")
    @Schema(description = "Уникальное имя сервиса", example = "billing-service")
    private String name;

    @Schema(description = "Признак критичности сервиса", example = "true")
    private boolean critical;
}
