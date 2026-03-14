package ru.kpfu.itis.efremov.schemarisk.core.engine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.kpfu.itis.efremov.schemarisk.model.CompatibilityMode;
import ru.kpfu.itis.efremov.schemarisk.model.Issue;

import java.util.List;
import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompatibilityResult {

    private boolean compatible;
    private CompatibilityMode mode;
    private List<Issue> issues;

    public List<Issue> getIssues() {
        return Objects.requireNonNullElse(issues, List.of());
    }
}
