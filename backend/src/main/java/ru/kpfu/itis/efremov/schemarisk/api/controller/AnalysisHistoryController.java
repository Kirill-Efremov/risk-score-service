package ru.kpfu.itis.efremov.schemarisk.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kpfu.itis.efremov.schemarisk.api.dto.AnalysisRecordResponse;
import ru.kpfu.itis.efremov.schemarisk.api.error.ApiErrorResponse;
import ru.kpfu.itis.efremov.schemarisk.history.service.GetAnalysisByIdService;

@Validated
@RestController
@RequestMapping("/api/v1/checks")
@Tag(name = "Analysis History", description = "История сохраненных анализов")
public class AnalysisHistoryController {

    private final GetAnalysisByIdService getAnalysisByIdService;

    public AnalysisHistoryController(GetAnalysisByIdService getAnalysisByIdService) {
        this.getAnalysisByIdService = getAnalysisByIdService;
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Получить анализ по ID",
            description = "Возвращает сохраненный результат анализа схемы"
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

