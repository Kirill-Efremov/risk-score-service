package ru.kpfu.itis.efremov.schemarisk.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Request for user registration")
public class RegisterRequest {

    @NotBlank(message = "username must not be blank")
    @Schema(description = "Unique username", example = "kirill")
    private String username;

    @NotBlank(message = "password must not be blank")
    @Size(min = 6, message = "password length must be at least 6 characters")
    @Schema(description = "Plain-text password", example = "password123", minLength = 6)
    private String password;
}
