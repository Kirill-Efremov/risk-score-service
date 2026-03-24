package ru.kpfu.itis.efremov.schemarisk.analysis.graph.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Ответ с графом использования схемы")
public class UsageGraphResponse {

    @Schema(description = "Узлы графа")
    private final List<GraphNodeDto> nodes;

    @Schema(description = "Ребра графа")
    private final List<GraphEdgeDto> edges;

    public UsageGraphResponse(List<GraphNodeDto> nodes, List<GraphEdgeDto> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public List<GraphNodeDto> getNodes() {
        return nodes;
    }

    public List<GraphEdgeDto> getEdges() {
        return edges;
    }
}
