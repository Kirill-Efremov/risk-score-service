package ru.kpfu.itis.efremov.schemarisk.history.model;

import ru.kpfu.itis.efremov.schemarisk.analysis.governance.SchemaPromotionStatus;

public record UpdatePromotionMetadataCommand(
        Long analysisId,
        Boolean promotionAttempted,
        Boolean registered,
        SchemaPromotionStatus registrationStatus,
        Integer registeredVersion,
        Integer schemaRegistryId
) {
}
