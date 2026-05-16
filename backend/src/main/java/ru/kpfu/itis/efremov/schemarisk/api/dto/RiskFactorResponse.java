package ru.kpfu.itis.efremov.schemarisk.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.kpfu.itis.efremov.schemarisk.analysis.risk.RiskFactor;

@Schema(description = "Фактор риска, который повлиял на итоговый riskScore")
public record RiskFactorResponse(
        @Schema(description = "Код фактора риска", example = "DIFF_FIELD_REMOVED")
        String code,
        @Schema(description = "Сообщение, объясняющее фактор риска", example = "Field 'email' was removed")
        String message,
        @Schema(description = "Вес фактора в итоговом riskScore", example = "25")
        int weight,
        @Schema(description = "Источник фактора риска", example = "DIFF")
        String source
) {
    public static RiskFactorResponse fromFactor(RiskFactor factor) {
        return new RiskFactorResponse(
                factor.code(),
                factor.message(),
                factor.weight(),
                factor.source().name()
        );
    }
}
