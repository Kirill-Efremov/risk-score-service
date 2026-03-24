package ru.kpfu.itis.efremov.schemarisk.core.recommend;

import org.springframework.stereotype.Service;
import ru.kpfu.itis.efremov.schemarisk.core.diff.DiffResult;
import ru.kpfu.itis.efremov.schemarisk.core.diff.FieldChange;
import ru.kpfu.itis.efremov.schemarisk.core.engine.CompatibilityResult;
import ru.kpfu.itis.efremov.schemarisk.core.risk.RiskResult;
import ru.kpfu.itis.efremov.schemarisk.model.Issue;
import ru.kpfu.itis.efremov.schemarisk.model.IssueSeverity;

import java.util.ArrayList;
import java.util.List;

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
}
