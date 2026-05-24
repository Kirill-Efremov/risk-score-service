package ru.kpfu.itis.efremov.schemarisk.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication response")
public record AuthResponse(
        @Schema(description = "JWT access token")
        String accessToken,
        @Schema(description = "Token type", example = "Bearer")
        String tokenType,
        @Schema(description = "Authenticated user")
        CurrentUserResponse user
) {
}
