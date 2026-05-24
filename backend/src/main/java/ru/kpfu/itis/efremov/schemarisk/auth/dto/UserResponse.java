package ru.kpfu.itis.efremov.schemarisk.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.kpfu.itis.efremov.schemarisk.auth.persistence.UserEntity;

import java.time.Instant;

@Schema(description = "Registered user response")
public record UserResponse(
        @Schema(description = "User ID", example = "1")
        Long id,
        @Schema(description = "Username", example = "kirill")
        String username,
        @Schema(description = "User role", example = "ADMIN")
        String role,
        @Schema(description = "Whether the user is active", example = "true")
        boolean active,
        @Schema(description = "Creation timestamp")
        Instant createdAt,
        @Schema(description = "Last update timestamp")
        Instant updatedAt
) {
    public static UserResponse fromEntity(UserEntity user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getRole().name(),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
