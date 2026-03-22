package ru.kpfu.itis.efremov.schemarisk.api.dto;

import ru.kpfu.itis.efremov.schemarisk.application.impact.model.ImpactResult;

import java.util.List;

public record ImpactResponse(
        int affectedConsumersCount,
        int affectedProducersCount,
        List<String> criticalServices,
        boolean breaking
) {
    public static ImpactResponse fromResult(ImpactResult impactResult) {
        if (impactResult == null) {
            return null;
        }
        return new ImpactResponse(
                impactResult.affectedConsumersCount(),
                impactResult.affectedProducersCount(),
                impactResult.criticalServices(),
                impactResult.breaking()
        );
    }
}
