package ru.kpfu.itis.efremov.schemarisk.analysis.governance;

import org.springframework.stereotype.Service;
import ru.kpfu.itis.efremov.schemarisk.analysis.compatibility.CompatibilityResult;
import ru.kpfu.itis.efremov.schemarisk.analysis.diff.DiffResult;
import ru.kpfu.itis.efremov.schemarisk.analysis.diff.FieldChange;
import ru.kpfu.itis.efremov.schemarisk.analysis.diff.FieldChangeType;
import ru.kpfu.itis.efremov.schemarisk.analysis.impact.ImpactResult;
import ru.kpfu.itis.efremov.schemarisk.analysis.risk.RiskResult;
import ru.kpfu.itis.efremov.schemarisk.common.model.Issue;
import ru.kpfu.itis.efremov.schemarisk.common.model.IssueSeverity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RecommendationService {

    public List<String> generateRecommendations(
            CompatibilityResult compatibilityResult,
            DiffResult diffResult,
            RiskResult riskResult
    ) {
        List<String> recommendations = new ArrayList<>();

        switch (riskResult.getRiskLevel()) {
            case HIGH -> recommendations.add(
                    "High risk change. Avoid deploying without a migration plan and coordination with consumers."
            );
            case MEDIUM -> recommendations.add(
                    "Medium risk change. Validate impact on key consumers and run targeted regression checks."
            );
            case LOW -> recommendations.add(
                    "Low risk change. Notify dependent teams and update schema documentation."
            );
        }

        for (Issue issue : compatibilityResult.getIssues()) {
            if (issue.getSeverity() == IssueSeverity.ERROR) {
                recommendations.add(
                        "Fix Avro compatibility issue: " + issue.getCode() + " - " + issue.getMessage()
                );
            }
        }

        if (diffResult != null) {
            for (FieldChange change : diffResult.getChanges()) {
                recommendations.addAll(recommendationsForChange(change));
            }
        }

        return recommendations;
    }

    public List<StructuredRecommendation> generateStructuredRecommendations(
            CompatibilityResult compatibilityResult,
            DiffResult diffResult,
            RiskResult riskResult,
            ImpactResult impactResult,
            GovernanceDecision governanceDecision
    ) {
        Map<String, StructuredRecommendation> recommendations = new LinkedHashMap<>();

        if (diffResult != null) {
            for (FieldChange change : diffResult.getChanges()) {
                StructuredRecommendation recommendation = recommendationForChange(change);
                if (recommendation != null) {
                    recommendations.putIfAbsent(recommendation.code() + ":" + recommendation.target(), recommendation);
                }
            }
        }

        StructuredRecommendation governanceRecommendation = recommendationForGovernance(governanceDecision);
        if (governanceRecommendation != null) {
            recommendations.putIfAbsent(governanceRecommendation.code(), governanceRecommendation);
        }

        if (impactResult != null && impactResult.affectedConsumersCount() > 0) {
            recommendations.putIfAbsent(
                    "IMPACT_NOTIFY_AFFECTED_CONSUMERS",
                    new StructuredRecommendation(
                            "IMPACT_NOTIFY_AFFECTED_CONSUMERS",
                            RecommendationSeverity.MEDIUM,
                            "consumers",
                            "Active consumers are affected by this schema change",
                            "Notify affected consumer owners and verify compatibility before promotion"
                    )
            );
        }

        if (impactResult != null && !impactResult.criticalServices().isEmpty()) {
            recommendations.putIfAbsent(
                    "IMPACT_VALIDATE_CRITICAL_SERVICES",
                    new StructuredRecommendation(
                            "IMPACT_VALIDATE_CRITICAL_SERVICES",
                            RecommendationSeverity.HIGH,
                            "critical-services",
                            "Critical services are affected",
                            "Run additional validation and obtain approval from critical service owners before promotion"
                    )
            );
        }

        return List.copyOf(recommendations.values());
    }

    private List<String> recommendationsForChange(FieldChange change) {
        return switch (change.getType()) {
            case REMOVED -> List.of(
                    "Field '" + change.getFieldName()
                            + "' was removed. Prefer deprecation or staged migration for consumers."
            );
            case TYPE_CHANGED -> List.of(
                    "Field '" + change.getFieldName()
                            + "' changed type from " + change.getOldType() + " to " + change.getNewType()
                            + ". Consider introducing a new field instead of mutating the old contract."
            );
            case REQUIRED_ADDED, OPTIONAL_BECAME_REQUIRED -> List.of(
                    "Field '" + change.getFieldName()
                            + "' became required. Add a default or keep it optional for backward compatibility."
            );
            case OPTIONAL_ADDED, ADDED -> List.of(
                    "Field '" + change.getFieldName()
                            + "' was added. Verify that its default and semantics are safe for existing consumers."
            );
            case DEFAULT_ADDED -> List.of(
                    "Field '" + change.getFieldName()
                            + "' received a default value. Confirm the default is correct for historical data."
            );
            case DEFAULT_REMOVED -> List.of(
                    "Field '" + change.getFieldName()
                            + "' lost its default value. This can effectively make the field required."
            );
            case DEFAULT_CHANGED -> List.of(
                    "Field '" + change.getFieldName()
                            + "' changed its default value. Check downstream assumptions about omitted data."
            );
            case NULLABILITY_CHANGED, REQUIRED_BECAME_OPTIONAL -> List.of(
                    "Field '" + change.getFieldName()
                            + "' changed optional/nullability semantics. Verify consumer expectations."
            );
            case NESTED_CHANGED -> List.of(
                    "Nested record field '" + change.getFieldName()
                            + "' changed internally. Review compatibility of the nested contract."
            );
            case OTHER -> List.of();
        };
    }

    private StructuredRecommendation recommendationForChange(FieldChange change) {
        String fieldName = change.getFieldName();
        return switch (change.getType()) {
            case REMOVED -> new StructuredRecommendation(
                    "FIELD_REMOVED_USE_DEPRECATION",
                    RecommendationSeverity.HIGH,
                    fieldName,
                    "Field '" + fieldName + "' was removed",
                    "Use staged deprecation: keep the field during a transition period, migrate consumers, and remove it only after consumers no longer depend on it"
            );
            case TYPE_CHANGED -> new StructuredRecommendation(
                    "FIELD_TYPE_CHANGED_ADD_NEW_FIELD",
                    RecommendationSeverity.HIGH,
                    fieldName,
                    "Field '" + fieldName + "' type was changed",
                    "Avoid changing the existing field type directly. Add a new field with the new type and migrate consumers gradually"
            );
            case REQUIRED_ADDED -> new StructuredRecommendation(
                    "REQUIRED_FIELD_ADDED_PROVIDE_DEFAULT",
                    RecommendationSeverity.HIGH,
                    fieldName,
                    "Required field '" + fieldName + "' was added",
                    "Provide a safe default value or make the field optional until all consumers are ready"
            );
            case OPTIONAL_BECAME_REQUIRED -> new StructuredRecommendation(
                    "OPTIONAL_FIELD_BECAME_REQUIRED_KEEP_OPTIONAL",
                    RecommendationSeverity.HIGH,
                    fieldName,
                    "Optional field '" + fieldName + "' became required",
                    "Keep the field optional during migration or ensure all producers and consumers support the new requirement"
            );
            case REQUIRED_BECAME_OPTIONAL -> new StructuredRecommendation(
                    "REQUIRED_FIELD_BECAME_OPTIONAL_REVIEW_CONSUMERS",
                    RecommendationSeverity.MEDIUM,
                    fieldName,
                    "Required field '" + fieldName + "' became optional",
                    "Review consumers that assume this field is always present"
            );
            case NULLABILITY_CHANGED -> new StructuredRecommendation(
                    "NULLABILITY_CHANGED_REVIEW_CONTRACT",
                    RecommendationSeverity.MEDIUM,
                    fieldName,
                    "Nullability of field '" + fieldName + "' changed",
                    "Review serialization/deserialization behavior and update consumers if necessary"
            );
            case DEFAULT_REMOVED -> new StructuredRecommendation(
                    "DEFAULT_REMOVED_RESTORE_OR_MIGRATE",
                    RecommendationSeverity.MEDIUM,
                    fieldName,
                    "Default value for field '" + fieldName + "' was removed",
                    "Restore a default value or migrate clients before removing it"
            );
            case DEFAULT_CHANGED -> new StructuredRecommendation(
                    "DEFAULT_CHANGED_REVIEW_SEMANTICS",
                    RecommendationSeverity.MEDIUM,
                    fieldName,
                    "Default value for field '" + fieldName + "' was changed",
                    "Check whether the new default changes business semantics for existing consumers"
            );
            case NESTED_CHANGED -> new StructuredRecommendation(
                    "NESTED_FIELD_CHANGED_REVIEW_STRUCTURE",
                    RecommendationSeverity.MEDIUM,
                    fieldName,
                    "Nested structure '" + fieldName + "' changed",
                    "Review nested schema evolution and compatibility with consumers"
            );
            case OPTIONAL_ADDED, ADDED -> new StructuredRecommendation(
                    "OPTIONAL_FIELD_ADDED_SAFE_CHANGE",
                    RecommendationSeverity.LOW,
                    fieldName,
                    "Optional field '" + fieldName + "' was added",
                    "This is usually a safe schema evolution step. Ensure consumers tolerate unknown fields"
            );
            case DEFAULT_ADDED -> new StructuredRecommendation(
                    "DEFAULT_ADDED_SAFE_CHANGE",
                    RecommendationSeverity.LOW,
                    fieldName,
                    "Default value for field '" + fieldName + "' was added",
                    "This usually improves backward compatibility. Verify that the default value matches business expectations"
            );
            case OTHER -> new StructuredRecommendation(
                    "SCHEMA_CHANGE_REVIEW_REQUIRED",
                    RecommendationSeverity.MEDIUM,
                    fieldName,
                    "Field '" + fieldName + "' changed",
                    "Review the schema change manually"
            );
        };
    }

    private StructuredRecommendation recommendationForGovernance(GovernanceDecision governanceDecision) {
        if (governanceDecision == null) {
            return null;
        }

        return switch (governanceDecision) {
            case REJECT -> new StructuredRecommendation(
                    "GOVERNANCE_REJECT_FIX_BREAKING_CHANGES",
                    RecommendationSeverity.HIGH,
                    "schema",
                    "Schema change was rejected by governance policy",
                    "Fix breaking changes or use a new subject/versioning strategy before promoting the schema"
            );
            case REQUIRE_CONSUMER_UPGRADE_FIRST -> new StructuredRecommendation(
                    "GOVERNANCE_UPGRADE_CONSUMERS_FIRST",
                    RecommendationSeverity.HIGH,
                    "consumers",
                    "Consumers must be upgraded before this schema change is promoted",
                    "Coordinate rollout: deploy compatible consumers first, then promote the new schema"
            );
            case ALLOW_WITH_CAUTION -> new StructuredRecommendation(
                    "GOVERNANCE_MANUAL_REVIEW_RECOMMENDED",
                    RecommendationSeverity.MEDIUM,
                    "schema",
                    "Schema change is allowed with caution",
                    "Perform manual review and verify affected services before promotion"
            );
            case SUGGEST_NEW_SUBJECT -> new StructuredRecommendation(
                    "GOVERNANCE_USE_NEW_SUBJECT",
                    RecommendationSeverity.HIGH,
                    "subject",
                    "Schema name changed significantly",
                    "Register this schema under a new subject instead of evolving the existing one"
            );
            case ALLOW -> null;
        };
    }
}
