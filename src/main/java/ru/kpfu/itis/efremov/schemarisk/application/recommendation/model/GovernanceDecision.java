package ru.kpfu.itis.efremov.schemarisk.application.recommendation.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Governance-решение по результатам анализа")
public enum GovernanceDecision {
    ALLOW,
    ALLOW_WITH_CAUTION,
    REQUIRE_CONSUMER_UPGRADE_FIRST,
    REJECT,
    SUGGEST_NEW_SUBJECT
}
