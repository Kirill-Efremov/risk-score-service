package ru.kpfu.itis.efremov.schemarisk.core.risk;

import org.springframework.stereotype.Component;
import ru.kpfu.itis.efremov.schemarisk.core.diff.FieldChange;
import ru.kpfu.itis.efremov.schemarisk.core.engine.CompatibilityResult;
import ru.kpfu.itis.efremov.schemarisk.model.Decision;
import ru.kpfu.itis.efremov.schemarisk.model.Issue;
import ru.kpfu.itis.efremov.schemarisk.model.IssueSeverity;

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
            switch (ch.getType()) {
                case REMOVED -> score += 30;
                case TYPE_CHANGED -> score += 30;
                case ADDED -> {
                    if (ch.getNewDefault() == null) {
                        score += 20;
                    } else {
                        score += 5;
                    }
                }
                case DEFAULT_ADDED -> score += 5;
                case DEFAULT_REMOVED -> score += 15;
                case OTHER -> score += 5;
            }
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
}
