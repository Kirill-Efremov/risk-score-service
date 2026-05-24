package ru.kpfu.itis.efremov.schemarisk.approval.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.kpfu.itis.efremov.schemarisk.approval.dto.SchemaApprovalResponse;
import ru.kpfu.itis.efremov.schemarisk.approval.model.SchemaApprovalStatus;
import ru.kpfu.itis.efremov.schemarisk.approval.service.SchemaApprovalService;
import ru.kpfu.itis.efremov.schemarisk.auth.service.CurrentUserService;
import ru.kpfu.itis.efremov.schemarisk.api.error.ApiErrorResponse;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/schema-approvals")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Schema Approvals", description = "Пользовательские заявки на согласование публикации схем.")
public class SchemaApprovalController {

    private final SchemaApprovalService schemaApprovalService;
    private final CurrentUserService currentUserService;

    public SchemaApprovalController(
            SchemaApprovalService schemaApprovalService,
            CurrentUserService currentUserService
    ) {
        this.schemaApprovalService = schemaApprovalService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/my")
    @Operation(summary = "Получить заявки текущего пользователя")
    public ResponseEntity<List<SchemaApprovalResponse>> listMyApprovals(
            @RequestParam(required = false) SchemaApprovalStatus status,
            @RequestParam(defaultValue = "50") @Min(1) @Max(200) Integer limit
    ) {
        return ResponseEntity.ok(
                schemaApprovalService.listMyApprovals(currentUserService.currentUsernameOptional().orElse(null), status, limit)
        );
    }

    @GetMapping("/{approvalId}")
    @Operation(summary = "Получить заявку на согласование")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Заявка найдена"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Заявка не найдена",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<SchemaApprovalResponse> getApproval(
            @Parameter(description = "ID заявки", example = "12")
            @PathVariable Long approvalId
    ) {
        return ResponseEntity.ok(
                schemaApprovalService.getApprovalForUser(approvalId, currentUserService.currentUsernameOptional().orElse(null))
        );
    }
}
