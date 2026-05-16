package ru.kpfu.itis.efremov.schemarisk.analysis.governance;

public record StructuredRecommendation(
        String code,
        RecommendationSeverity severity,
        String target,
        String message,
        String action
) {
}
