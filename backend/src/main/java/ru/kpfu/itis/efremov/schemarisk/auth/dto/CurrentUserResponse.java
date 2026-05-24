package ru.kpfu.itis.efremov.schemarisk.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.kpfu.itis.efremov.schemarisk.auth.persistence.UserEntity;

@Schema(description = "Current user response")
public record CurrentUserResponse(
        @Schema(description = "User ID", example = "1")
        Long id,
        @Schema(description = "Username", example = "kirill")
        String username,
        @Schema(description = "User role", example = "ADMIN")
        String role,
        @Schema(description = "Whether the user is active", example = "true")
        boolean active
) {
    public static CurrentUserResponse fromEntity(UserEntity user) {
        return new CurrentUserResponse(
                user.getId(),
                user.getUsername(),
                user.getRole().name(),
                user.isActive()
        );
    }
}
