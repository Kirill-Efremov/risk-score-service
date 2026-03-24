package ru.kpfu.itis.efremov.schemarisk.analysis.graph.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ребро графа использования схемы")
public class GraphEdgeDto {

    @Schema(description = "Идентификатор исходного узла", example = "schema:user-created")
    private final String from;

    @Schema(description = "Идентификатор целевого узла", example = "service:billing-service")
    private final String to;

    @Schema(description = "Тип связи: PRODUCER или CONSUMER", example = "PRODUCER")
    private final String type;

    public GraphEdgeDto(String from, String to, String type) {
        this.from = from;
        this.to = to;
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getType() {
        return type;
    }
}
