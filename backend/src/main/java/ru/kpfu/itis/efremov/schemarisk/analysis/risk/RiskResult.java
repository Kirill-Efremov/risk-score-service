package ru.kpfu.itis.efremov.schemarisk.analysis.risk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.kpfu.itis.efremov.schemarisk.common.model.Decision;

import java.util.List;
import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskResult {

    private int riskScore;
    private RiskLevel riskLevel;
    private Decision decision;
    @Builder.Default
    private List<RiskFactor> riskFactors = List.of();

    public List<RiskFactor> getRiskFactors() {
        return Objects.requireNonNullElse(riskFactors, List.of());
    }
}
