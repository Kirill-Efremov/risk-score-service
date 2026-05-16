package ru.kpfu.itis.efremov.schemarisk.analysis.risk;

import org.springframework.stereotype.Component;
import ru.kpfu.itis.efremov.schemarisk.analysis.compatibility.CompatibilityResult;
import ru.kpfu.itis.efremov.schemarisk.analysis.diff.FieldChange;
import ru.kpfu.itis.efremov.schemarisk.analysis.diff.FieldChangeType;
import ru.kpfu.itis.efremov.schemarisk.analysis.impact.ImpactResult;
import ru.kpfu.itis.efremov.schemarisk.common.model.Decision;
import ru.kpfu.itis.efremov.schemarisk.common.model.Issue;
import ru.kpfu.itis.efremov.schemarisk.common.model.IssueSeverity;

import java.util.ArrayList;
import java.util.List;

@Component
public class RiskScorer {

    public RiskResult score(CompatibilityResult compatibilityResult, List<FieldChange> changes) {
        return score(compatibilityResult, changes, null);
    }

    public RiskResult score(
            CompatibilityResult compatibilityResult,
            List<FieldChange> changes,
            ImpactResult impactResult
    ) {
        List<RiskFactor> riskFactors = new ArrayList<>();
        List<FieldChange> safeChanges = changes != null ? changes : List.of();

        addCompatibilityFactors(riskFactors, compatibilityResult);
        addDiffFactors(riskFactors, safeChanges);
        addImpactFactors(riskFactors, compatibilityResult, safeChanges, impactResult);

        int riskScore = clampScore(riskFactors.stream()
                .mapToInt(RiskFactor::weight)
                .sum());
        RiskLevel riskLevel = resolveRiskLevel(riskScore);

        return RiskResult.builder()
                .riskScore(riskScore)
                .riskLevel(riskLevel)
                .decision(resolveDecision(riskLevel))
                .riskFactors(List.copyOf(riskFactors))
                .build();
    }

    private void addCompatibilityFactors(List<RiskFactor> riskFactors, CompatibilityResult compatibilityResult) {
        if (compatibilityResult == null) {
            return;
        }

        if (!compatibilityResult.isCompatible()) {
            riskFactors.add(new RiskFactor(
                    "COMPATIBILITY_BREAKING_CHANGE",
                    "Schema change is not formally compatible",
                    40,
                    RiskFactorSource.COMPATIBILITY
            ));
        }

        for (Issue issue : compatibilityResult.getIssues()) {
            riskFactors.add(new RiskFactor(
                    "COMPATIBILITY_ISSUE",
                    buildCompatibilityMessage(issue),
                    issue.getSeverity() == IssueSeverity.ERROR ? 20 : 10,
                    RiskFactorSource.COMPATIBILITY
            ));
        }
    }

    private void addDiffFactors(List<RiskFactor> riskFactors, List<FieldChange> changes) {
        for (FieldChange change : changes) {
            riskFactors.add(new RiskFactor(
                    diffCode(change.getChangeType()),
                    buildDiffMessage(change),
                    diffWeight(change.getChangeType()),
                    RiskFactorSource.DIFF
            ));
        }
    }

    private void addImpactFactors(
            List<RiskFactor> riskFactors,
            CompatibilityResult compatibilityResult,
            List<FieldChange> changes,
            ImpactResult impactResult
    ) {
        if (impactResult == null) {
            return;
        }

        if (impactResult.affectedConsumersCount() > 0) {
            int consumerCount = impactResult.affectedConsumersCount();
            riskFactors.add(new RiskFactor(
                    "IMPACT_ACTIVE_CONSUMERS",
                    buildConsumerImpactMessage(consumerCount),
                    Math.min(consumerCount * 4, 20),
                    RiskFactorSource.IMPACT
            ));
        }

        if (impactResult.affectedProducersCount() > 0) {
            int producerCount = impactResult.affectedProducersCount();
            riskFactors.add(new RiskFactor(
                    "IMPACT_PRODUCERS",
                    buildProducerImpactMessage(producerCount),
                    Math.min(producerCount * 2, 10),
                    RiskFactorSource.IMPACT
            ));
        }

        if (!impactResult.criticalServices().isEmpty()) {
            riskFactors.add(new RiskFactor(
                    "IMPACT_CRITICAL_SERVICES",
                    "Critical services are affected: " + String.join(", ", impactResult.criticalServices()),
                    25,
                    RiskFactorSource.IMPACT
            ));
        }

        if ((impactResult.breaking() || isBreakingChange(compatibilityResult, changes))
                && impactResult.affectedConsumersCount() > 0) {
            riskFactors.add(new RiskFactor(
                    "IMPACT_BREAKING_WITH_CONSUMERS",
                    "Breaking change affects active consumers",
                    20,
                    RiskFactorSource.IMPACT
            ));
        }
    }

    private boolean isBreakingChange(CompatibilityResult compatibilityResult, List<FieldChange> changes) {
        boolean compatibilityBreaking = compatibilityResult != null
                && (!compatibilityResult.isCompatible()
                || compatibilityResult.getIssues().stream().anyMatch(Issue::isBreakingChange));

        boolean diffBreaking = changes.stream()
                .map(FieldChange::getChangeType)
                .anyMatch(changeType -> changeType == FieldChangeType.REMOVED
                        || changeType == FieldChangeType.TYPE_CHANGED
                        || changeType == FieldChangeType.REQUIRED_ADDED
                        || changeType == FieldChangeType.OPTIONAL_BECAME_REQUIRED);

        return compatibilityBreaking || diffBreaking;
    }

    private String buildCompatibilityMessage(Issue issue) {
        if (issue == null) {
            return "Compatibility issue detected";
        }
        if (issue.getMessage() != null && !issue.getMessage().isBlank()) {
            return issue.getMessage();
        }
        if (issue.getCode() != null && !issue.getCode().isBlank()) {
            return "Compatibility issue: " + issue.getCode();
        }
        return "Compatibility issue detected";
    }

    private int diffWeight(FieldChangeType changeType) {
        if (changeType == null) {
            return 5;
        }

        return switch (changeType) {
            case REMOVED, TYPE_CHANGED, REQUIRED_ADDED -> 25;
            case OPTIONAL_BECAME_REQUIRED -> 20;
            case REQUIRED_BECAME_OPTIONAL, NULLABILITY_CHANGED, DEFAULT_REMOVED, NESTED_CHANGED -> 10;
            case DEFAULT_CHANGED -> 8;
            case OPTIONAL_ADDED -> 2;
            case DEFAULT_ADDED -> 1;
            case OTHER, ADDED -> 5;
        };
    }

    private String diffCode(FieldChangeType changeType) {
        if (changeType == null) {
            return "DIFF_OTHER";
        }

        return switch (changeType) {
            case REMOVED -> "DIFF_FIELD_REMOVED";
            case TYPE_CHANGED -> "DIFF_FIELD_TYPE_CHANGED";
            case REQUIRED_ADDED -> "DIFF_REQUIRED_FIELD_ADDED";
            case OPTIONAL_BECAME_REQUIRED -> "DIFF_OPTIONAL_BECAME_REQUIRED";
            case REQUIRED_BECAME_OPTIONAL -> "DIFF_REQUIRED_BECAME_OPTIONAL";
            case NULLABILITY_CHANGED -> "DIFF_NULLABILITY_CHANGED";
            case DEFAULT_REMOVED -> "DIFF_DEFAULT_REMOVED";
            case DEFAULT_CHANGED -> "DIFF_DEFAULT_CHANGED";
            case NESTED_CHANGED -> "DIFF_NESTED_CHANGED";
            case OPTIONAL_ADDED -> "DIFF_OPTIONAL_FIELD_ADDED";
            case DEFAULT_ADDED -> "DIFF_DEFAULT_ADDED";
            case OTHER, ADDED -> "DIFF_OTHER";
        };
    }

    private String buildDiffMessage(FieldChange change) {
        String fieldName = change.getFieldName() != null ? change.getFieldName() : "unknown";
        FieldChangeType changeType = change.getChangeType();
        if (changeType == null) {
            return "Field '" + fieldName + "' was changed";
        }

        return switch (changeType) {
            case REMOVED -> "Field '" + fieldName + "' was removed";
            case TYPE_CHANGED -> "Field '" + fieldName + "' type was changed"
                    + formatTypeDetails(change.getOldType(), change.getNewType());
            case REQUIRED_ADDED -> "Required field '" + fieldName + "' was added";
            case OPTIONAL_BECAME_REQUIRED -> "Optional field '" + fieldName + "' became required";
            case REQUIRED_BECAME_OPTIONAL -> "Required field '" + fieldName + "' became optional";
            case NULLABILITY_CHANGED -> "Field '" + fieldName + "' nullability was changed";
            case DEFAULT_REMOVED -> "Default value was removed from field '" + fieldName + "'";
            case DEFAULT_CHANGED -> "Default value was changed for field '" + fieldName + "'"
                    + formatDefaultDetails(change.getOldDefault(), change.getNewDefault());
            case NESTED_CHANGED -> "Field '" + fieldName + "' has nested changes";
            case OPTIONAL_ADDED, ADDED -> "Optional field '" + fieldName + "' was added";
            case DEFAULT_ADDED -> "Default value was added for field '" + fieldName + "'";
            case OTHER -> "Field '" + fieldName + "' was changed";
        };
    }

    private String formatTypeDetails(String oldType, String newType) {
        if (oldType == null || newType == null) {
            return "";
        }
        return " from '" + normalize(oldType) + "' to '" + normalize(newType) + "'";
    }

    private String formatDefaultDetails(String oldDefault, String newDefault) {
        if (oldDefault == null || newDefault == null) {
            return "";
        }
        return " from '" + normalize(oldDefault) + "' to '" + normalize(newDefault) + "'";
    }

    private String normalize(String value) {
        return value.replace('\n', ' ').trim();
    }

    private String buildConsumerImpactMessage(int consumerCount) {
        if (consumerCount == 1) {
            return "1 active consumer depends on this schema";
        }
        return consumerCount + " active consumers depend on this schema";
    }

    private String buildProducerImpactMessage(int producerCount) {
        if (producerCount == 1) {
            return "1 producer publishes events with this schema";
        }
        return producerCount + " producers publish events with this schema";
    }

    private int clampScore(int score) {
        return Math.max(0, Math.min(score, 100));
    }

    private RiskLevel resolveRiskLevel(int score) {
        if (score >= 70) {
            return RiskLevel.HIGH;
        }
        if (score >= 30) {
            return RiskLevel.MEDIUM;
        }
        return RiskLevel.LOW;
    }

    private Decision resolveDecision(RiskLevel riskLevel) {
        return switch (riskLevel) {
            case LOW -> Decision.ALLOW;
            case MEDIUM -> Decision.WARN;
            case HIGH -> Decision.BLOCK;
        };
    }
}
