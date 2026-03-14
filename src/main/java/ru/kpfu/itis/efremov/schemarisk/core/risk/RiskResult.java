package ru.kpfu.itis.efremov.schemarisk.core.risk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.kpfu.itis.efremov.schemarisk.model.Decision;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskResult {

    private int riskScore;
    private RiskLevel riskLevel;
    private Decision decision;
}
