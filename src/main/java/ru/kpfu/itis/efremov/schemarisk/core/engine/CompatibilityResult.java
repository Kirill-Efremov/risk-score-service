package ru.kpfu.itis.efremov.schemarisk.core.engine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.kpfu.itis.efremov.schemarisk.model.CompatibilityMode;
import ru.kpfu.itis.efremov.schemarisk.model.Issue;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompatibilityResult {

    private boolean compatible;              // итог по выбранному режиму
    private CompatibilityMode mode;          // BACKWARD / FORWARD / FULL / NONE
    private List<Issue> issues;              // найденные проблемы (ERROR/WARNING)
}
