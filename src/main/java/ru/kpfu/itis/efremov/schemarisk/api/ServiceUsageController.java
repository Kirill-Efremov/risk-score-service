package ru.kpfu.itis.efremov.schemarisk.api;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kpfu.itis.efremov.schemarisk.api.dto.ApiErrorResponse;
import ru.kpfu.itis.efremov.schemarisk.api.dto.RegisterServiceRequest;
import ru.kpfu.itis.efremov.schemarisk.api.dto.RegisterServiceUsageRequest;
import ru.kpfu.itis.efremov.schemarisk.api.dto.ServiceResponse;
import ru.kpfu.itis.efremov.schemarisk.api.dto.ServiceUsageResponse;
import ru.kpfu.itis.efremov.schemarisk.api.dto.UpdateServiceUsageStatusRequest;
import ru.kpfu.itis.efremov.schemarisk.application.usage.model.RegisterServiceCommand;
import ru.kpfu.itis.efremov.schemarisk.application.usage.model.RegisterServiceUsageCommand;
import ru.kpfu.itis.efremov.schemarisk.application.usage.service.ServiceUsageService;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Service Usage", description = "Управление использованием схем сервисами")
public class ServiceUsageController {

    private final ServiceUsageService serviceUsageService;

    public ServiceUsageController(ServiceUsageService serviceUsageService) {
        this.serviceUsageService = serviceUsageService;
    }

    @PostMapping("/services")
    @Operation(summary = "Регистрация сервиса")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Некорректный запрос",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ресурс не найден",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Конфликт состояния",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ServiceResponse> registerService(@Valid @RequestBody RegisterServiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ServiceResponse.fromInfo(
                        serviceUsageService.registerService(
                                new RegisterServiceCommand(request.getName(), request.isCritical())
                        )
                )
        );
    }

    @PostMapping("/services/{serviceId}/usages")
    @Operation(summary = "Регистрация использования схемы сервисом")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Некорректный запрос",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ресурс не найден",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Конфликт состояния",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ServiceUsageResponse> registerUsage(
            @Parameter(description = "ID сервиса", example = "10")
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
                                )
                        )
                )
        );
    }

    @GetMapping("/subjects/{subject}/usages")
    @Operation(
            summary = "Получить usages по subject",
            description = "Возвращает список зарегистрированных usages для указанного subject"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Некорректный запрос",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ресурс не найден",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Конфликт состояния",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<List<ServiceUsageResponse>> getUsageBySubject(
            @Parameter(description = "Имя subject (например: user-created)", example = "user-created")
            @PathVariable @NotBlank(message = "subject must not be blank") String subject
    ) {
        return ResponseEntity.ok(
                serviceUsageService.getUsageBySubject(subject).stream()
                        .map(ServiceUsageResponse::fromInfo)
                        .toList()
        );
    }

    @PatchMapping("/services/usages/{usageId}/status")
    @Operation(
            summary = "Обновить статус usage",
            description = "Меняет статус ACTIVE → MIGRATING → DEPRECATED"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Некорректный запрос",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ресурс не найден",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Конфликт состояния",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ServiceUsageResponse> updateUsageStatus(
            @Parameter(description = "ID usage", example = "101")
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
