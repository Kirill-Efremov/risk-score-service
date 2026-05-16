package ru.kpfu.itis.efremov.schemarisk.analysis.risk;

public record RiskFactor(
        String code,
        String message,
        int weight,
        RiskFactorSource source
) {
}
