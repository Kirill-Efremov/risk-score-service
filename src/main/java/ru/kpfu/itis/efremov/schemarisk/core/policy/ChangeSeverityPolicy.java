package ru.kpfu.itis.efremov.schemarisk.core.policy;

import ru.kpfu.itis.efremov.schemarisk.core.diff.FieldChangeType;
import ru.kpfu.itis.efremov.schemarisk.model.IssueSeverity;

public interface ChangeSeverityPolicy {

    IssueSeverity severityOf(FieldChangeType changeType);

    default boolean isBreaking(FieldChangeType changeType) {
        return severityOf(changeType) == IssueSeverity.ERROR;
    }
}
