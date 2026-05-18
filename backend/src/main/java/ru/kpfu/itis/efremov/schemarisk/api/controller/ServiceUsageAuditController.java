package ru.kpfu.itis.efremov.schemarisk.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.kpfu.itis.efremov.schemarisk.api.dto.ServiceUsageAuditResponse;
import ru.kpfu.itis.efremov.schemarisk.usage.audit.model.ServiceUsageAuditAction;
import ru.kpfu.itis.efremov.schemarisk.usage.service.ServiceUsageService;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1")
@Tag(
        name = "Service Usage Audit",
        description = "History of service usage map changes"
)
public class ServiceUsageAuditController {

    private final ServiceUsageService serviceUsageService;

    public ServiceUsageAuditController(ServiceUsageService serviceUsageService) {
        this.serviceUsageService = serviceUsageService;
    }

    @GetMapping("/services/{serviceId}/audit")
    @Operation(summary = "Get audit history for a service")
    public ResponseEntity<List<ServiceUsageAuditResponse>> getServiceAudit(
            @PathVariable @Positive(message = "serviceId must be positive") Long serviceId,
            @RequestParam(required = false) ServiceUsageAuditAction action,
            @RequestParam(required = false) Integer limit
    ) {
        return ResponseEntity.ok(
                serviceUsageService.getServiceAudit(serviceId, action, limit).stream()
                        .map(ServiceUsageAuditResponse::fromRecord)
                        .toList()
        );
    }

    @GetMapping("/services/{serviceId}/usages/{usageId}/audit")
    @Operation(summary = "Get audit history for a usage link")
    public ResponseEntity<List<ServiceUsageAuditResponse>> getUsageAudit(
            @PathVariable @Positive(message = "serviceId must be positive") Long serviceId,
            @PathVariable @Positive(message = "usageId must be positive") Long usageId,
            @RequestParam(required = false) Integer limit
    ) {
        return ResponseEntity.ok(
                serviceUsageService.getUsageAudit(serviceId, usageId, limit).stream()
                        .map(ServiceUsageAuditResponse::fromRecord)
                        .toList()
        );
    }

    @GetMapping("/usage-audit")
    @Operation(summary = "Get global usage audit log")
    public ResponseEntity<List<ServiceUsageAuditResponse>> getUsageAuditLog(
            @RequestParam(required = false) Long serviceId,
            @RequestParam(required = false) Long usageId,
            @RequestParam(required = false) ServiceUsageAuditAction action,
            @RequestParam(required = false) Integer limit
    ) {
        return ResponseEntity.ok(
                serviceUsageService.searchAudit(serviceId, usageId, action, limit).stream()
                        .map(ServiceUsageAuditResponse::fromRecord)
                        .toList()
        );
    }
}
