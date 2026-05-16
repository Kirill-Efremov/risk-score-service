package ru.kpfu.itis.efremov.schemarisk.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kpfu.itis.efremov.schemarisk.analysis.model.SchemaAnalysisInput;
import ru.kpfu.itis.efremov.schemarisk.analysis.model.SchemaAnalysisResult;
import ru.kpfu.itis.efremov.schemarisk.analysis.service.SchemaAnalysisExecutor;
import ru.kpfu.itis.efremov.schemarisk.api.dto.SchemaAnalysisResponse;
import ru.kpfu.itis.efremov.schemarisk.api.dto.SchemaCheckRequest;
import ru.kpfu.itis.efremov.schemarisk.api.error.ApiErrorResponse;

@Validated
@RestController
@RequestMapping("/api/v1/checks")
@Tag(
        name = "Schema Analysis (Raw)",
        description = "Standalone-анализ двух raw-схем без обращения к Schema Registry и без публикации."
)
public class SchemaCheckController {

    private final SchemaAnalysisExecutor schemaAnalysisExecutor;

    public SchemaCheckController(SchemaAnalysisExecutor schemaAnalysisExecutor) {
        this.schemaAnalysisExecutor = schemaAnalysisExecutor;
    }

    @PostMapping
    @Operation(
            summary = "Standalone-анализ двух raw-схем",
            description = "Сравнивает oldSchema и newSchema напрямую, выполняет compatibility analysis, diff, "
                    + "risk scoring и формирует рекомендации. Не использует Schema Registry и не публикует схему."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Результат raw-анализа",
                    content = @Content(
                            schema = @Schema(implementation = SchemaAnalysisResponse.class),
                            examples = @ExampleObject(
                                    name = "raw-analysis-safe-optional-field",
                                    value = """
                                            {
                                              "compatible": true,
                                              "mode": "BACKWARD",
                                              "riskScore": 2,
                                              "riskLevel": "LOW",
                                              "decision": "ALLOW",
                                              "governanceDecision": null,
                                              "decisionExplanation": [],
                                              "impact": null,
                                              "impactGraph": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<SchemaAnalysisResponse> check(@Valid @RequestBody SchemaCheckRequest request) {
        SchemaAnalysisResult result = schemaAnalysisExecutor.execute(
                new SchemaAnalysisInput(
                        request.getSchemaType(),
                        request.getCompatibilityMode(),
                        request.getOldSchema(),
                        request.getNewSchema()
                )
        );
        return ResponseEntity.ok(
                SchemaAnalysisResponse.fromResult(
                        result.compatibilityResult(),
                        result.diffResult(),
                        result.riskResult(),
                        result.governanceDecision(),
                        result.decisionExplanation(),
                        result.recommendations(),
                        result.structuredRecommendations(),
                        result.impact(),
                        result.impactGraph(),
                        request.getOldSchema(),
                        request.getNewSchema()
                )
        );
    }
}
