package ru.kpfu.itis.efremov.schemarisk.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.kpfu.itis.efremov.schemarisk.api.error.ApiErrorResponse;
import ru.kpfu.itis.efremov.schemarisk.auth.dto.UpdateUserRequest;
import ru.kpfu.itis.efremov.schemarisk.auth.dto.UserResponse;
import ru.kpfu.itis.efremov.schemarisk.auth.model.UserRole;
import ru.kpfu.itis.efremov.schemarisk.auth.service.AdminUserService;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin Users", description = "Управление пользователями администратором")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    @Operation(summary = "Получить список пользователей")
    public ResponseEntity<List<UserResponse>> listUsers(
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) Boolean active
    ) {
        return ResponseEntity.ok(adminUserService.listUsers(role, active));
    }

    @PatchMapping("/{userId}")
    @Operation(summary = "Изменить роль или активность пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь обновлен"),
            @ApiResponse(responseCode = "400", description = "Недопустимое изменение пользователя",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable @Positive(message = "userId must be positive") Long userId,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return ResponseEntity.ok(adminUserService.updateUser(userId, request));
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Деактивировать пользователя",
            description = "Soft-delete: пользователь остается в базе. Нельзя деактивировать самого себя и последнего активного администратора.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь деактивирован"),
            @ApiResponse(responseCode = "400", description = "Недопустимая деактивация пользователя",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<UserResponse> deactivateUser(
            @PathVariable @Positive(message = "userId must be positive") Long userId
    ) {
        return ResponseEntity.ok(adminUserService.deactivateUser(userId));
    }
}
