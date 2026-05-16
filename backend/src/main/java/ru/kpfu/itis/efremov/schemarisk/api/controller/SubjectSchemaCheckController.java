package ru.kpfu.itis.efremov.schemarisk.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kpfu.itis.efremov.schemarisk.analysis.model.AnalyzeVersionedSchemaChangeCommand;
import ru.kpfu.itis.efremov.schemarisk.analysis.model.SchemaAnalysisResult;
import ru.kpfu.itis.efremov.schemarisk.analysis.service.AnalyzeVersionedSchemaChangeService;
import ru.kpfu.itis.efremov.schemarisk.api.dto.AnalysisRecordResponse;
import ru.kpfu.itis.efremov.schemarisk.api.dto.SchemaAnalysisResponse;
import ru.kpfu.itis.efremov.schemarisk.api.dto.VersionedSchemaCheckRequest;
import ru.kpfu.itis.efremov.schemarisk.api.error.ApiErrorResponse;
import ru.kpfu.itis.efremov.schemarisk.history.service.ListSubjectAnalysesService;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/subjects")
@Tag(name = "Schema Analysis (Versioned)", description = "Versioned-анализ схем относительно данных из Schema Registry.")
public class SubjectSchemaCheckController {

    private final AnalyzeVersionedSchemaChangeService analyzeVersionedSchemaChangeService;
    private final ListSubjectAnalysesService listSubjectAnalysesService;

    public SubjectSchemaCheckController(
            AnalyzeVersionedSchemaChangeService analyzeVersionedSchemaChangeService,
            ListSubjectAnalysesService listSubjectAnalysesService
    ) {
        this.analyzeVersionedSchemaChangeService = analyzeVersionedSchemaChangeService;
        this.listSubjectAnalysesService = listSubjectAnalysesService;
    }

    @PostMapping("/{subject}/checks")
    @Operation(
            summary = "Versioned-анализ схемы относительно версии из Schema Registry без публикации",
            description = "Сравнивает версии схем из Schema Registry или latest/oldVersion из Registry с новой draft-схемой. "
                    + "Новая схема не публикуется автоматически."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Результат versioned-анализа",
                    content = @Content(
                            schema = @Schema(implementation = SchemaAnalysisResponse.class),
                            examples = @ExampleObject(
                                    name = "versioned-analysis-with-impact-graph",
                                    value = """
                                            {
                                              "compatible": false,
                                              "mode": "BACKWARD",
                                              "issues": [],
                                              "diff": null,
                                              "riskScore": 65,
                                              "riskLevel": "MEDIUM",
                                              "decision": "WARN",
                                              "governanceDecision": "REQUIRE_CONSUMER_UPGRADE_FIRST",
                                              "decisionExplanation": [
                                                "Breaking change detected",
                                                "2 active consumers found"
                                              ],
                                              "recommendations": [
                                                "Coordinate consumer updates before rollout"
                                              ],
                                              "impact": {
                                                "affectedConsumersCount": 2,
                                                "affectedProducersCount": 1,
                                                "criticalServices": [
                                                  "billing-service"
                                                ],
                                                "breaking": true
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ресурс не найден",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Конфликт состояния",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<SchemaAnalysisResponse> check(
            @Parameter(description = "Имя subject", example = "user-created")
            @PathVariable @NotBlank(message = "subject must not be blank") String subject,
            @Valid @RequestBody VersionedSchemaCheckRequest request
    ) {
        SchemaAnalysisResult result = analyzeVersionedSchemaChangeService.analyze(
                new AnalyzeVersionedSchemaChangeCommand(
                        subject,
                        request.getOldVersion(),
                        request.getNewVersion(),
                        request.getNewSchema(),
                        request.getSchemaType(),
                        request.getCompatibilityMode(),
                        null,
                        false
                )
        );

        return ResponseEntity.ok(SchemaAnalysisResponse.fromResult(result));
    }

    @GetMapping("/{subject}/checks")
    @Operation(
            summary = "Получить историю анализов по subject",
            description = "Возвращает сохраненные результаты versioned-анализа для указанного subject."
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
    public ResponseEntity<List<AnalysisRecordResponse>> listBySubject(
            @Parameter(description = "Имя subject", example = "user-created")
            @PathVariable @NotBlank(message = "subject must not be blank") String subject
    ) {
        return ResponseEntity.ok(
                listSubjectAnalysesService.listBySubject(subject).stream()
                        .map(AnalysisRecordResponse::fromRecord)
                        .toList()
        );
    }
}
