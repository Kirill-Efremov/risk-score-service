package ru.kpfu.itis.efremov.schemarisk.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import ru.kpfu.itis.efremov.schemarisk.auth.model.UserRole;

@Data
@Schema(description = "Admin request for user update")
public class UpdateUserRequest {

    @Schema(description = "Target user role", example = "ADMIN")
    private UserRole role;

    @Schema(description = "Whether the user is active", example = "true")
    private Boolean active;
}
