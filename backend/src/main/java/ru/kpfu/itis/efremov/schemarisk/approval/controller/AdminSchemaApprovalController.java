package ru.kpfu.itis.efremov.schemarisk.approval.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.kpfu.itis.efremov.schemarisk.api.error.ApiErrorResponse;
import ru.kpfu.itis.efremov.schemarisk.approval.dto.ApprovalDecisionRequest;
import ru.kpfu.itis.efremov.schemarisk.approval.dto.SchemaApprovalResponse;
import ru.kpfu.itis.efremov.schemarisk.approval.model.SchemaApprovalStatus;
import ru.kpfu.itis.efremov.schemarisk.approval.service.SchemaApprovalService;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/admin/schema-approvals")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(
        name = "Admin Schema Approvals",
        description = "Администратор может согласовать только формально совместимые изменения, "
                + "для которых governanceDecision = ALLOW_WITH_CAUTION."
)
public class AdminSchemaApprovalController {

    private final SchemaApprovalService schemaApprovalService;

    public AdminSchemaApprovalController(SchemaApprovalService schemaApprovalService) {
        this.schemaApprovalService = schemaApprovalService;
    }

    @GetMapping
    @Operation(summary = "Получить список заявок на согласование")
    public ResponseEntity<List<SchemaApprovalResponse>> listApprovals(
            @RequestParam(required = false) SchemaApprovalStatus status,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String requestedBy,
            @RequestParam(defaultValue = "100") @Min(1) @Max(200) Integer limit
    ) {
        return ResponseEntity.ok(schemaApprovalService.listAdminApprovals(status, subject, requestedBy, limit));
    }

    @GetMapping("/{approvalId}")
    @Operation(summary = "Получить заявку на согласование по ID")
    public ResponseEntity<SchemaApprovalResponse> getApproval(@PathVariable Long approvalId) {
        return ResponseEntity.ok(schemaApprovalService.getAdminApproval(approvalId));
    }

    @PostMapping("/{approvalId}/approve")
    @Operation(
            summary = "Согласовать публикацию схемы",
            description = "Администратор не может согласовать формально несовместимую схему."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Схема опубликована"),
            @ApiResponse(responseCode = "400", description = "Согласование недоступно для этого состояния",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Базовая версия изменилась или Schema Registry отклонил публикацию",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<SchemaApprovalResponse> approve(
            @PathVariable Long approvalId,
            @Valid @RequestBody(required = false) ApprovalDecisionRequest request
    ) {
        return ResponseEntity.ok(schemaApprovalService.approve(approvalId, request));
    }

    @PostMapping("/{approvalId}/reject")
    @Operation(summary = "Отклонить заявку на публикацию схемы")
    public ResponseEntity<SchemaApprovalResponse> reject(
            @PathVariable Long approvalId,
            @Valid @RequestBody(required = false) ApprovalDecisionRequest request
    ) {
        return ResponseEntity.ok(schemaApprovalService.reject(approvalId, request));
    }
}
