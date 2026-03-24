package ru.kpfu.itis.efremov.schemarisk.analysis.compatibility;

import ru.kpfu.itis.efremov.schemarisk.analysis.diff.FieldChangeType;
import ru.kpfu.itis.efremov.schemarisk.common.model.IssueSeverity;

public interface ChangeSeverityPolicy {

    IssueSeverity severityOf(FieldChangeType changeType);

    default boolean isBreaking(FieldChangeType changeType) {
        return severityOf(changeType) == IssueSeverity.ERROR;
    }
}




