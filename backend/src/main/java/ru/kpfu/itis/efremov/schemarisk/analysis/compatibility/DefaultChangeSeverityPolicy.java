package ru.kpfu.itis.efremov.schemarisk.analysis.compatibility;

import org.springframework.stereotype.Component;
import ru.kpfu.itis.efremov.schemarisk.analysis.diff.FieldChangeType;
import ru.kpfu.itis.efremov.schemarisk.common.model.IssueSeverity;

@Component
public class DefaultChangeSeverityPolicy implements ChangeSeverityPolicy {

    @Override
    public IssueSeverity severityOf(FieldChangeType changeType) {
        return switch (changeType) {
            case TYPE_CHANGED, REMOVED, REQUIRED_ADDED, OPTIONAL_BECAME_REQUIRED,
                    ENUM_RESTRICTED, CONST_CHANGED, ADDITIONAL_PROPERTIES_DISABLED,
                    STRING_CONSTRAINT_TIGHTENED, NUMERIC_CONSTRAINT_TIGHTENED, ARRAY_CONSTRAINT_CHANGED,
                    COMPOSITION_CHANGED, FIELD_LABEL_CHANGED, FIELD_NUMBER_REUSED,
                    FIELD_REMOVED_WITHOUT_RESERVED, ONEOF_REMOVED, FIELD_MOVED_TO_ONEOF,
                    FIELD_REMOVED_FROM_ONEOF, ENUM_VALUE_REMOVED, ENUM_VALUE_NUMBER_REUSED,
                    MAP_TYPE_CHANGED, WIRE_TYPE_CHANGED -> IssueSeverity.ERROR;
            case REQUIRED_BECAME_OPTIONAL, NULLABILITY_CHANGED, DEFAULT_CHANGED, DEFAULT_REMOVED, NESTED_CHANGED,
                    ENUM_EXPANDED, ADDITIONAL_PROPERTIES_ENABLED, STRING_CONSTRAINT_RELAXED,
                    NUMERIC_CONSTRAINT_RELAXED, FIELD_NAME_CHANGED, RESERVED_NUMBER_ADDED,
                    RESERVED_NUMBER_REMOVED, ONEOF_ADDED, ENUM_VALUE_ADDED, ENUM_VALUE_NAME_CHANGED ->
                    IssueSeverity.WARNING;
            case OPTIONAL_ADDED, DEFAULT_ADDED, ADDED, OTHER -> IssueSeverity.INFO;
        };
    }
}




