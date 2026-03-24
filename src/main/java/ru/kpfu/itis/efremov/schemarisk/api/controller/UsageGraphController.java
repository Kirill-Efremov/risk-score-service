package ru.kpfu.itis.efremov.schemarisk.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kpfu.itis.efremov.schemarisk.analysis.graph.UsageGraphService;
import ru.kpfu.itis.efremov.schemarisk.analysis.graph.dto.UsageGraphResponse;
import ru.kpfu.itis.efremov.schemarisk.api.error.ApiErrorResponse;

@Validated
@RestController
@RequestMapping("/api/v1/subjects")
@Tag(name = "Usage Graph", description = "Граф использования схемы продюсерами и консюмерами")
public class UsageGraphController {

    private final UsageGraphService usageGraphService;

    public UsageGraphController(UsageGraphService usageGraphService) {
        this.usageGraphService = usageGraphService;
    }

    @GetMapping("/{subject}/graph")
    @Operation(
            summary = "Получить граф использования схемы",
            description = "Возвращает узлы и ребра графа для сервисов, использующих указанный subject."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Граф использования",
                    content = @Content(
                            schema = @Schema(implementation = UsageGraphResponse.class),
                            examples = @ExampleObject(
                                    name = "usage-graph",
                                    value = """
                                            {
                                              "nodes": [
                                                {
                                                  "id": "schema:user-created",
                                                  "type": "SCHEMA",
                                                  "label": "user-created",
                                                  "impact": null,
                                                  "critical": false
                                                },
                                                {
                                                  "id": "service:billing-service",
                                                  "type": "SERVICE",
                                                  "label": "billing-service",
                                                  "impact": "SAFE",
                                                  "critical": true
                                                },
                                                {
                                                  "id": "service:notification-service",
                                                  "type": "SERVICE",
                                                  "label": "notification-service",
                                                  "impact": "SAFE",
                                                  "critical": false
                                                }
                                              ],
                                              "edges": [
                                                {
                                                  "from": "schema:user-created",
                                                  "to": "service:billing-service",
                                                  "type": "PRODUCER"
                                                },
                                                {
                                                  "from": "schema:user-created",
                                                  "to": "service:notification-service",
                                                  "type": "CONSUMER"
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ресурс не найден",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public UsageGraphResponse getGraph(
            @Parameter(description = "Имя subject", example = "user-created")
            @PathVariable @NotBlank(message = "subject must not be blank") String subject
    ) {
        return usageGraphService.buildGraph(subject);
    }
}
