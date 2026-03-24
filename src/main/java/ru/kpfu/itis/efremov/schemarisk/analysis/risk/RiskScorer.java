package ru.kpfu.itis.efremov.schemarisk.analysis.risk;

import org.springframework.stereotype.Component;
import ru.kpfu.itis.efremov.schemarisk.analysis.diff.FieldChange;
import ru.kpfu.itis.efremov.schemarisk.analysis.compatibility.CompatibilityResult;
import ru.kpfu.itis.efremov.schemarisk.common.model.Decision;
import ru.kpfu.itis.efremov.schemarisk.common.model.Issue;
import ru.kpfu.itis.efremov.schemarisk.common.model.IssueSeverity;

import java.util.List;

@Component
public class RiskScorer {

    public RiskResult score(CompatibilityResult compatibilityResult, List<FieldChange> changes) {
        int score = 0;

        for (Issue issue : compatibilityResult.getIssues()) {
            if (issue.getSeverity() == IssueSeverity.ERROR) {
                score += 40;
            }
        }

        for (FieldChange ch : changes) {
            score += scoreBySeverity(ch.getSeverity());
            score += scoreByChangeType(ch);
        }

        if (score < 0) score = 0;
        if (score > 100) score = 100;

        RiskLevel level;
        Decision decision;

        if (score >= 70) {
            level = RiskLevel.HIGH;
            decision = Decision.BLOCK;
        } else if (score >= 30) {
            level = RiskLevel.MEDIUM;
            decision = Decision.WARN;
        } else {
            level = RiskLevel.LOW;
            decision = Decision.ALLOW;
        }

        return RiskResult.builder()
                .riskScore(score)
                .riskLevel(level)
                .decision(decision)
                .build();
    }

    private int scoreBySeverity(IssueSeverity severity) {
        if (severity == null) {
            return 0;
        }

        return switch (severity) {
            case ERROR -> 20;
            case WARNING -> 10;
            case INFO -> 3;
        };
    }

    private int scoreByChangeType(FieldChange change) {
        return switch (change.getChangeType()) {
            case REMOVED, TYPE_CHANGED, REQUIRED_ADDED, OPTIONAL_BECAME_REQUIRED -> 20;
            case REQUIRED_BECAME_OPTIONAL, NULLABILITY_CHANGED, DEFAULT_REMOVED, DEFAULT_CHANGED, NESTED_CHANGED -> 10;
            case OPTIONAL_ADDED, DEFAULT_ADDED, ADDED -> 5;
            case OTHER -> 3;
        };
    }
}




