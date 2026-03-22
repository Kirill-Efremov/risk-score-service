package ru.kpfu.itis.efremov.schemarisk.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterServiceRequest {

    @NotBlank(message = "name must not be blank")
    private String name;

    private boolean critical;
}
