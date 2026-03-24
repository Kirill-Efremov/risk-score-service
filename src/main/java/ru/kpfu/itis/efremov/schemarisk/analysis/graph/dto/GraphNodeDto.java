package ru.kpfu.itis.efremov.schemarisk.analysis.graph.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Узел графа использования схемы")
public class GraphNodeDto {

    @Schema(description = "Уникальный идентификатор узла", example = "schema:user-created")
    private final String id;

    @Schema(description = "Тип узла: SCHEMA или SERVICE", example = "SCHEMA")
    private final String type;

    @Schema(description = "Человекочитаемая подпись узла", example = "billing-service")
    private final String label;

    @Schema(description = "Маркер влияния для сервисного узла", example = "BREAKING", nullable = true)
    private final String impact;

    @Schema(description = "Признак критичности сервиса", example = "true")
    private final boolean critical;

    public GraphNodeDto(String id, String type, String label, String impact, boolean critical) {
        this.id = id;
        this.type = type;
        this.label = label;
        this.impact = impact;
        this.critical = critical;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getLabel() {
        return label;
    }

    public String getImpact() {
        return impact;
    }

    public boolean isCritical() {
        return critical;
    }
}
