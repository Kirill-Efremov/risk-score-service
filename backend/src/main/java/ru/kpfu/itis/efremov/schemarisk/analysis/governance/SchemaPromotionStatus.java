package ru.kpfu.itis.efremov.schemarisk.analysis.governance;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Promotion result status")
public enum SchemaPromotionStatus {
    REGISTERED,
    BLOCKED_BY_GOVERNANCE,
    REQUIRES_MANUAL_APPROVAL,
    REQUIRES_CONSUMER_UPGRADE,
    SUGGEST_NEW_SUBJECT,
    REGISTRY_REJECTED,
    ANALYSIS_ONLY
}
