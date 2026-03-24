package ru.kpfu.itis.efremov.schemarisk.analysis.compatibility;

import org.springframework.stereotype.Component;
import ru.kpfu.itis.efremov.schemarisk.analysis.diff.FieldChangeType;
import ru.kpfu.itis.efremov.schemarisk.common.model.IssueSeverity;

@Component
public class DefaultChangeSeverityPolicy implements ChangeSeverityPolicy {

    @Override
    public IssueSeverity severityOf(FieldChangeType changeType) {
        return switch (changeType) {
            case TYPE_CHANGED, REMOVED, REQUIRED_ADDED, OPTIONAL_BECAME_REQUIRED -> IssueSeverity.ERROR;
            case REQUIRED_BECAME_OPTIONAL, NULLABILITY_CHANGED, DEFAULT_CHANGED, DEFAULT_REMOVED, NESTED_CHANGED ->
                    IssueSeverity.WARNING;
            case OPTIONAL_ADDED, DEFAULT_ADDED, ADDED, OTHER -> IssueSeverity.INFO;
        };
    }
}




