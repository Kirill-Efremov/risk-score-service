package ru.kpfu.itis.efremov.schemarisk.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kpfu.itis.efremov.schemarisk.api.dto.AnalysisRecordResponse;
import ru.kpfu.itis.efremov.schemarisk.api.dto.SchemaCheckRequest;
import ru.kpfu.itis.efremov.schemarisk.api.dto.SchemaCheckResponse;
import ru.kpfu.itis.efremov.schemarisk.api.error.ApiErrorResponse;
import ru.kpfu.itis.efremov.schemarisk.analysis.model.AnalyzeSchemaChangeCommand;
import ru.kpfu.itis.efremov.schemarisk.analysis.model.AnalyzeSchemaChangeResult;
import ru.kpfu.itis.efremov.schemarisk.analysis.service.AnalyzeSchemaChangeService;
import ru.kpfu.itis.efremov.schemarisk.history.service.GetAnalysisByIdService;

@Validated
@RestController
@RequestMapping("/api/v1/checks")
@Tag(name = "Schema Analysis (Raw)", description = "Анализ изменения схемы по raw JSON")
public class SchemaCheckController {

    private final AnalyzeSchemaChangeService analyzeSchemaChangeService;
    private final GetAnalysisByIdService getAnalysisByIdService;

    public SchemaCheckController(
            AnalyzeSchemaChangeService analyzeSchemaChangeService,
            GetAnalysisByIdService getAnalysisByIdService
    ) {
        this.analyzeSchemaChangeService = analyzeSchemaChangeService;
        this.getAnalysisByIdService = getAnalysisByIdService;
    }

    @PostMapping
    @Operation(
            summary = "Анализ изменения схемы (raw)",
            description = "Проверяет совместимость, рассчитывает риск и governance-решение для переданных схем."
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
    public ResponseEntity<SchemaCheckResponse> check(@Valid @RequestBody SchemaCheckRequest request) {
        AnalyzeSchemaChangeResult result = analyzeSchemaChangeService.analyze(
                new AnalyzeSchemaChangeCommand(
                        request.getSchemaType(),
                        request.getCompatibilityMode(),
                        request.getOldSchema(),
                        request.getNewSchema()
                )
        );

        return ResponseEntity.ok(SchemaCheckResponse.fromResult(result));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Получить анализ по ID",
            description = "Возвращает сохранённый результат анализа схемы"
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
    public ResponseEntity<AnalysisRecordResponse> getById(
            @Parameter(description = "ID анализа", example = "42")
            @PathVariable @Positive(message = "id must be positive") Long id
    ) {
        return ResponseEntity.ok(AnalysisRecordResponse.fromRecord(getAnalysisByIdService.getById(id)));
    }
}




