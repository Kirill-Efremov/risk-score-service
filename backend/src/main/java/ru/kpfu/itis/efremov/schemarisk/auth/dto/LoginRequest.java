package ru.kpfu.itis.efremov.schemarisk.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request for user login")
public class LoginRequest {

    @NotBlank(message = "username must not be blank")
    @Schema(description = "Username", example = "kirill")
    private String username;

    @NotBlank(message = "password must not be blank")
    @Schema(description = "Plain-text password", example = "password123")
    private String password;
}
