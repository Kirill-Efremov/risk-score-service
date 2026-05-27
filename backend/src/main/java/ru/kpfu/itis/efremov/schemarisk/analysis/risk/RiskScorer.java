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
                        || changeType == FieldChangeType.OPTIONAL_BECAME_REQUIRED
                        || diffWeight(changeType) >= 20);

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
            case FIELD_NUMBER_REUSED, WIRE_TYPE_CHANGED, ENUM_VALUE_NUMBER_REUSED -> 35;
            case REMOVED, TYPE_CHANGED, REQUIRED_ADDED, FIELD_REMOVED_WITHOUT_RESERVED, MAP_TYPE_CHANGED,
                    FIELD_MOVED_TO_ONEOF, FIELD_REMOVED_FROM_ONEOF, ONEOF_REMOVED -> 25;
            case OPTIONAL_BECAME_REQUIRED, ENUM_RESTRICTED, CONST_CHANGED, ADDITIONAL_PROPERTIES_DISABLED,
                    STRING_CONSTRAINT_TIGHTENED, NUMERIC_CONSTRAINT_TIGHTENED, ARRAY_CONSTRAINT_CHANGED,
                    COMPOSITION_CHANGED, FIELD_LABEL_CHANGED, ENUM_VALUE_REMOVED -> 20;
            case REQUIRED_BECAME_OPTIONAL, NULLABILITY_CHANGED, DEFAULT_REMOVED, NESTED_CHANGED,
                    FIELD_NAME_CHANGED, RESERVED_NUMBER_REMOVED, ONEOF_ADDED, ENUM_VALUE_NAME_CHANGED -> 10;
            case ENUM_EXPANDED, ADDITIONAL_PROPERTIES_ENABLED, STRING_CONSTRAINT_RELAXED,
                    NUMERIC_CONSTRAINT_RELAXED, RESERVED_NUMBER_ADDED, ENUM_VALUE_ADDED -> 6;
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
            case ENUM_RESTRICTED -> "JSON_ENUM_RESTRICTED";
            case ENUM_EXPANDED -> "JSON_ENUM_EXPANDED";
            case CONST_CHANGED -> "JSON_CONST_CHANGED";
            case ADDITIONAL_PROPERTIES_DISABLED -> "JSON_ADDITIONAL_PROPERTIES_DISABLED";
            case ADDITIONAL_PROPERTIES_ENABLED -> "JSON_ADDITIONAL_PROPERTIES_ENABLED";
            case STRING_CONSTRAINT_TIGHTENED -> "JSON_CONSTRAINT_TIGHTENED";
            case STRING_CONSTRAINT_RELAXED -> "JSON_CONSTRAINT_RELAXED";
            case NUMERIC_CONSTRAINT_TIGHTENED -> "JSON_CONSTRAINT_TIGHTENED";
            case NUMERIC_CONSTRAINT_RELAXED -> "JSON_CONSTRAINT_RELAXED";
            case ARRAY_CONSTRAINT_CHANGED -> "JSON_ARRAY_CONSTRAINT_CHANGED";
            case COMPOSITION_CHANGED -> "JSON_COMPOSITION_CHANGED";
            case FIELD_NAME_CHANGED -> "PROTO_FIELD_NAME_CHANGED";
            case FIELD_LABEL_CHANGED -> "PROTO_FIELD_LABEL_CHANGED";
            case FIELD_NUMBER_REUSED -> "PROTO_FIELD_NUMBER_REUSED";
            case FIELD_REMOVED_WITHOUT_RESERVED -> "PROTO_FIELD_REMOVED_WITHOUT_RESERVED";
            case RESERVED_NUMBER_ADDED -> "PROTO_RESERVED_NUMBER_ADDED";
            case RESERVED_NUMBER_REMOVED -> "PROTO_RESERVED_NUMBER_REMOVED";
            case ONEOF_ADDED -> "PROTO_ONEOF_ADDED";
            case ONEOF_REMOVED -> "PROTO_ONEOF_CHANGED";
            case FIELD_MOVED_TO_ONEOF -> "PROTO_ONEOF_CHANGED";
            case FIELD_REMOVED_FROM_ONEOF -> "PROTO_ONEOF_CHANGED";
            case ENUM_VALUE_ADDED -> "PROTO_ENUM_VALUE_ADDED";
            case ENUM_VALUE_REMOVED -> "PROTO_ENUM_VALUE_REMOVED";
            case ENUM_VALUE_NUMBER_REUSED -> "PROTO_ENUM_NUMBER_REUSED";
            case ENUM_VALUE_NAME_CHANGED -> "PROTO_ENUM_VALUE_NAME_CHANGED";
            case MAP_TYPE_CHANGED -> "PROTO_MAP_TYPE_CHANGED";
            case WIRE_TYPE_CHANGED -> "PROTO_WIRE_TYPE_CHANGED";
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
            case ENUM_RESTRICTED -> "Allowed enum values for '" + fieldName + "' were restricted";
            case ENUM_EXPANDED -> "Allowed enum values for '" + fieldName + "' were expanded";
            case CONST_CHANGED -> "Const value for '" + fieldName + "' was changed";
            case ADDITIONAL_PROPERTIES_DISABLED -> "Additional JSON properties were disabled at '" + fieldName + "'";
            case ADDITIONAL_PROPERTIES_ENABLED -> "Additional JSON properties were enabled at '" + fieldName + "'";
            case STRING_CONSTRAINT_TIGHTENED -> "String constraints were tightened for '" + fieldName + "'";
            case STRING_CONSTRAINT_RELAXED -> "String constraints were relaxed for '" + fieldName + "'";
            case NUMERIC_CONSTRAINT_TIGHTENED -> "Numeric constraints were tightened for '" + fieldName + "'";
            case NUMERIC_CONSTRAINT_RELAXED -> "Numeric constraints were relaxed for '" + fieldName + "'";
            case ARRAY_CONSTRAINT_CHANGED -> "Array constraints or item type changed for '" + fieldName + "'";
            case COMPOSITION_CHANGED -> "JSON Schema composition changed for '" + fieldName + "'";
            case FIELD_NAME_CHANGED -> "Protobuf field name changed at '" + fieldName + "'";
            case FIELD_LABEL_CHANGED -> "Protobuf field label changed at '" + fieldName + "'";
            case FIELD_NUMBER_REUSED -> "Protobuf field number was reused at '" + fieldName + "'";
            case FIELD_REMOVED_WITHOUT_RESERVED -> "Protobuf field was removed without reserving its number: '" + fieldName + "'";
            case RESERVED_NUMBER_ADDED -> "Protobuf reserved number was added at '" + fieldName + "'";
            case RESERVED_NUMBER_REMOVED -> "Protobuf reserved number was removed at '" + fieldName + "'";
            case ONEOF_ADDED -> "Protobuf oneof was added at '" + fieldName + "'";
            case ONEOF_REMOVED -> "Protobuf oneof was removed at '" + fieldName + "'";
            case FIELD_MOVED_TO_ONEOF -> "Protobuf field was moved into oneof at '" + fieldName + "'";
            case FIELD_REMOVED_FROM_ONEOF -> "Protobuf field was removed from oneof at '" + fieldName + "'";
            case ENUM_VALUE_ADDED -> "Protobuf enum value was added at '" + fieldName + "'";
            case ENUM_VALUE_REMOVED -> "Protobuf enum value was removed at '" + fieldName + "'";
            case ENUM_VALUE_NUMBER_REUSED -> "Protobuf enum number was reused at '" + fieldName + "'";
            case ENUM_VALUE_NAME_CHANGED -> "Protobuf enum value name changed at '" + fieldName + "'";
            case MAP_TYPE_CHANGED -> "Protobuf map key/value type changed at '" + fieldName + "'";
            case WIRE_TYPE_CHANGED -> "Protobuf wire type changed at '" + fieldName + "'";
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
