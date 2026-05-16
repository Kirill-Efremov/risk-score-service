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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kpfu.itis.efremov.schemarisk.analysis.service.SchemaPromotionService;
import ru.kpfu.itis.efremov.schemarisk.api.dto.RegisterSchemaVersionRequest;
import ru.kpfu.itis.efremov.schemarisk.api.dto.SchemaPromotionRequest;
import ru.kpfu.itis.efremov.schemarisk.api.dto.SchemaPromotionResponse;
import ru.kpfu.itis.efremov.schemarisk.api.dto.SchemaVersionResponse;
import ru.kpfu.itis.efremov.schemarisk.api.error.ApiErrorResponse;
import ru.kpfu.itis.efremov.schemarisk.catalog.model.RegisterSchemaVersionCommand;
import ru.kpfu.itis.efremov.schemarisk.catalog.service.GetSchemaVersionService;
import ru.kpfu.itis.efremov.schemarisk.catalog.service.ListSchemaVersionsService;
import ru.kpfu.itis.efremov.schemarisk.catalog.service.RegisterSchemaVersionService;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/subjects")
@Tag(
        name = "Schema Catalog",
        description = "Операции с версиями схем в настроенном каталоге. По умолчанию сервис работает в "
                + "registry-first режиме и использует Schema Registry как источник истины."
)
public class SchemaCatalogController {

    private final RegisterSchemaVersionService registerSchemaVersionService;
    private final ListSchemaVersionsService listSchemaVersionsService;
    private final GetSchemaVersionService getSchemaVersionService;
    private final SchemaPromotionService schemaPromotionService;

    public SchemaCatalogController(
            RegisterSchemaVersionService registerSchemaVersionService,
            ListSchemaVersionsService listSchemaVersionsService,
            GetSchemaVersionService getSchemaVersionService,
            SchemaPromotionService schemaPromotionService
    ) {
        this.registerSchemaVersionService = registerSchemaVersionService;
        this.listSchemaVersionsService = listSchemaVersionsService;
        this.getSchemaVersionService = getSchemaVersionService;
        this.schemaPromotionService = schemaPromotionService;
    }

    @GetMapping
    @Operation(summary = "Получить список subjects из Schema Registry")
    public ResponseEntity<List<String>> listSubjects() {
        return ResponseEntity.ok(listSchemaVersionsService.getSubjects());
    }

    @PostMapping("/{subject}/versions")
    @Operation(summary = "Ручная регистрация версии схемы в Schema Registry через сервис")
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
    public ResponseEntity<SchemaVersionResponse> registerVersion(
            @Parameter(description = "Имя subject", example = "user-created")
            @PathVariable @NotBlank(message = "subject must not be blank") String subject,
            @Valid @RequestBody RegisterSchemaVersionRequest request
    ) {
        SchemaVersionResponse response = SchemaVersionResponse.fromInfo(
                registerSchemaVersionService.register(
                        new RegisterSchemaVersionCommand(
                                subject,
                                request.getSchemaType(),
                                request.getDefaultCompatibilityMode(),
                                request.getDescription(),
                                request.getSchemaText(),
                                request.getStatus(),
                                request.getSourceType(),
                                request.getExternalSchemaId()
                        )
                )
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{subject}/promotions")
    @Operation(
            summary = "Контролируемая публикация схемы",
            description = "Анализирует новую схему относительно latest-версии в Schema Registry и регистрирует "
                    + "ее только если governance policy разрешает публикацию."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Результат controlled promotion flow"),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ресурс не найден",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Конфликт состояния",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<SchemaPromotionResponse> promoteSchema(
            @Parameter(description = "Имя subject", example = "user-created")
            @PathVariable @NotBlank(message = "subject must not be blank") String subject,
            @Valid @RequestBody SchemaPromotionRequest request
    ) {
        return ResponseEntity.ok(schemaPromotionService.promote(subject, request));
    }

    @GetMapping("/{subject}/versions")
    @Operation(summary = "Получить список версий схемы из Schema Registry")
    public ResponseEntity<List<SchemaVersionResponse>> listVersions(
            @Parameter(description = "Имя subject", example = "user-created")
            @PathVariable @NotBlank(message = "subject must not be blank") String subject
    ) {
        List<SchemaVersionResponse> response = listSchemaVersionsService.getVersions(subject)
                .stream()
                .map(SchemaVersionResponse::fromInfo)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{subject}/versions/{version}")
    @Operation(summary = "Получить конкретную версию схемы из Schema Registry")
    public ResponseEntity<SchemaVersionResponse> getVersion(
            @Parameter(description = "Имя subject", example = "user-created")
            @PathVariable @NotBlank(message = "subject must not be blank") String subject,
            @Parameter(description = "Номер версии схемы", example = "2")
            @PathVariable @Positive(message = "version must be positive") int version
    ) {
        SchemaVersionResponse response = SchemaVersionResponse.fromInfo(
                getSchemaVersionService.getVersion(subject, version)
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{subject}/versions/latest")
    @Operation(summary = "Получить latest версию схемы из Schema Registry")
    public ResponseEntity<SchemaVersionResponse> getLatestVersion(
            @Parameter(description = "Имя subject", example = "user-created")
            @PathVariable @NotBlank(message = "subject must not be blank") String subject
    ) {
        SchemaVersionResponse response = SchemaVersionResponse.fromInfo(
                getSchemaVersionService.getLatestVersion(subject)
        );
        return ResponseEntity.ok(response);
    }
}
