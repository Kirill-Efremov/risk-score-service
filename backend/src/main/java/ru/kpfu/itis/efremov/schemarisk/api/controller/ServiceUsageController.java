package ru.kpfu.itis.efremov.schemarisk.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.kpfu.itis.efremov.schemarisk.api.dto.CreateServiceRequest;
import ru.kpfu.itis.efremov.schemarisk.api.dto.MigrateServiceUsageRequest;
import ru.kpfu.itis.efremov.schemarisk.api.dto.RegisterServiceUsageRequest;
import ru.kpfu.itis.efremov.schemarisk.api.dto.ServiceResponse;
import ru.kpfu.itis.efremov.schemarisk.api.dto.ServiceUsageResponse;
import ru.kpfu.itis.efremov.schemarisk.api.dto.UpdateServiceRequest;
import ru.kpfu.itis.efremov.schemarisk.api.dto.UpdateServiceUsageRequest;
import ru.kpfu.itis.efremov.schemarisk.api.dto.UpdateServiceUsageStatusRequest;
import ru.kpfu.itis.efremov.schemarisk.api.error.ApiErrorResponse;
import ru.kpfu.itis.efremov.schemarisk.usage.model.RegisterServiceCommand;
import ru.kpfu.itis.efremov.schemarisk.usage.model.RegisterServiceUsageCommand;
import ru.kpfu.itis.efremov.schemarisk.usage.model.ServiceRole;
import ru.kpfu.itis.efremov.schemarisk.usage.service.ServiceUsageService;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Service Usage", description = "Service and schema usage management")
public class ServiceUsageController {

    private final ServiceUsageService serviceUsageService;

    public ServiceUsageController(ServiceUsageService serviceUsageService) {
        this.serviceUsageService = serviceUsageService;
    }

    @PostMapping("/services")
    @Operation(summary = "Create a service")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Resource not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflict",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ServiceResponse> registerService(@Valid @RequestBody CreateServiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ServiceResponse.fromInfo(
                        serviceUsageService.registerService(
                                new RegisterServiceCommand(
                                        request.getName(),
                                        request.isCritical(),
                                        request.getOwner(),
                                        request.getDescription()
                                ),
                                request.getCreatedBy()
                        )
                )
        );
    }

    @GetMapping("/services")
    @Operation(summary = "List services")
    public ResponseEntity<List<ServiceResponse>> listServices(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Boolean critical
    ) {
        return ResponseEntity.ok(
                serviceUsageService.listServices(active, critical).stream()
                        .map(ServiceResponse::fromInfo)
                        .toList()
        );
    }

    @GetMapping("/services/{serviceId}")
    @Operation(summary = "Get service details")
    public ResponseEntity<ServiceResponse> getService(
            @PathVariable @Positive(message = "serviceId must be positive") Long serviceId
    ) {
        return ResponseEntity.ok(ServiceResponse.fromInfo(serviceUsageService.getService(serviceId)));
    }

    @PatchMapping("/services/{serviceId}")
    @Operation(summary = "Partially update a service")
    public ResponseEntity<ServiceResponse> updateService(
            @PathVariable @Positive(message = "serviceId must be positive") Long serviceId,
            @RequestBody UpdateServiceRequest request
    ) {
        return ResponseEntity.ok(
                ServiceResponse.fromInfo(
                        serviceUsageService.updateService(
                                serviceId,
                                request.getName(),
                                request.getCritical(),
                                request.getActive(),
                                request.getOwner(),
                                request.getDescription(),
                                request.getChangedBy()
                        )
                )
        );
    }

    @DeleteMapping("/services/{serviceId}")
    @Operation(summary = "Deactivate a service")
    public ResponseEntity<Void> deactivateService(
            @PathVariable @Positive(message = "serviceId must be positive") Long serviceId
    ) {
        serviceUsageService.deactivateService(serviceId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/services/{serviceId}/usages")
    @Operation(summary = "Create a usage link for a service")
    public ResponseEntity<ServiceUsageResponse> registerUsage(
            @Parameter(description = "Service ID", example = "10")
            @PathVariable @Positive(message = "serviceId must be positive") Long serviceId,
            @Valid @RequestBody RegisterServiceUsageRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ServiceUsageResponse.fromInfo(
                        serviceUsageService.registerUsage(
                                new RegisterServiceUsageCommand(
                                        serviceId,
                                        request.getSubject(),
                                        request.getVersion(),
                                        request.getRole(),
                                        request.getActive()
                                ),
                                request.getCreatedBy()
                        )
                )
        );
    }

    @GetMapping("/services/{serviceId}/usages")
    @Operation(summary = "List usage links for a service")
    public ResponseEntity<List<ServiceUsageResponse>> listServiceUsages(
            @PathVariable @Positive(message = "serviceId must be positive") Long serviceId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) ServiceRole role,
            @RequestParam(required = false) String subject
    ) {
        return ResponseEntity.ok(
                serviceUsageService.listServiceUsages(serviceId, active, role, subject).stream()
                        .map(ServiceUsageResponse::fromInfo)
                        .toList()
        );
    }

    @PatchMapping("/services/{serviceId}/usages/{usageId}")
    @Operation(summary = "Partially update a usage link")
    public ResponseEntity<ServiceUsageResponse> updateUsage(
            @PathVariable @Positive(message = "serviceId must be positive") Long serviceId,
            @PathVariable @Positive(message = "usageId must be positive") Long usageId,
            @Valid @RequestBody UpdateServiceUsageRequest request
    ) {
        return ResponseEntity.ok(
                ServiceUsageResponse.fromInfo(
                        serviceUsageService.updateUsage(
                                serviceId,
                                usageId,
                                request.getSubject(),
                                request.getVersion(),
                                request.getRole(),
                                request.getActive(),
                                request.getChangedBy()
                        )
                )
        );
    }

    @DeleteMapping("/services/{serviceId}/usages/{usageId}")
    @Operation(summary = "Deactivate a usage link")
    public ResponseEntity<Void> deactivateUsage(
            @PathVariable @Positive(message = "serviceId must be positive") Long serviceId,
            @PathVariable @Positive(message = "usageId must be positive") Long usageId
    ) {
        serviceUsageService.deactivateUsage(serviceId, usageId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/services/{serviceId}/usages/{usageId}/migrate")
    @Operation(summary = "Migrate a usage link to another schema version")
    public ResponseEntity<ServiceUsageResponse> migrateUsage(
            @PathVariable @Positive(message = "serviceId must be positive") Long serviceId,
            @PathVariable @Positive(message = "usageId must be positive") Long usageId,
            @Valid @RequestBody MigrateServiceUsageRequest request
    ) {
        return ResponseEntity.ok(
                ServiceUsageResponse.fromInfo(
                        serviceUsageService.migrateUsage(
                                serviceId,
                                usageId,
                                request.getTargetVersion(),
                                request.getChangedBy()
                        )
                )
        );
    }

    @GetMapping("/subjects/{subject}/usages")
    @Operation(summary = "Get usages for a subject")
    public ResponseEntity<List<ServiceUsageResponse>> getUsageBySubject(
            @Parameter(description = "Subject name", example = "user-created")
            @PathVariable @NotBlank(message = "subject must not be blank") String subject
    ) {
        return ResponseEntity.ok(
                serviceUsageService.getUsageBySubject(subject).stream()
                        .map(ServiceUsageResponse::fromInfo)
                        .toList()
        );
    }

    @PatchMapping("/services/usages/{usageId}/status")
    @Operation(summary = "Update the lifecycle status of a usage")
    public ResponseEntity<ServiceUsageResponse> updateUsageStatus(
            @Parameter(description = "Usage ID", example = "101")
            @PathVariable @Positive(message = "usageId must be positive") Long usageId,
            @Valid @RequestBody UpdateServiceUsageStatusRequest request
    ) {
        return ResponseEntity.ok(
                ServiceUsageResponse.fromInfo(
                        serviceUsageService.updateUsageStatus(usageId, request.getStatus())
                )
        );
    }
}
