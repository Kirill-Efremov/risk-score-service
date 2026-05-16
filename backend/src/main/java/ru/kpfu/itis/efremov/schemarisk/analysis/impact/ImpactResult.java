package ru.kpfu.itis.efremov.schemarisk.analysis.impact;

import java.util.List;

public record ImpactResult(
        int affectedConsumersCount,
        int affectedProducersCount,
        List<String> criticalServices,
        boolean breaking
) {
}




