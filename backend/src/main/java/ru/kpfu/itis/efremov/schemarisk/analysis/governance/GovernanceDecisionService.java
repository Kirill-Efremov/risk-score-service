package ru.kpfu.itis.efremov.schemarisk.analysis.governance;

import org.springframework.stereotype.Service;
import ru.kpfu.itis.efremov.schemarisk.analysis.compatibility.CompatibilityResult;
import ru.kpfu.itis.efremov.schemarisk.analysis.diff.DiffResult;
import ru.kpfu.itis.efremov.schemarisk.analysis.diff.FieldChangeType;
import ru.kpfu.itis.efremov.schemarisk.analysis.impact.ImpactResult;
import ru.kpfu.itis.efremov.schemarisk.analysis.risk.RiskFactor;
import ru.kpfu.itis.efremov.schemarisk.analysis.risk.RiskLevel;
import ru.kpfu.itis.efremov.schemarisk.analysis.risk.RiskResult;
import ru.kpfu.itis.efremov.schemarisk.common.model.Issue;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class GovernanceDecisionService {

    public GovernanceDecision decide(
            CompatibilityResult compatibilityResult,
            DiffResult diffResult,
            RiskResult riskResult,
            ImpactResult impactResult
    ) {
        return decide(compatibilityResult, diffResult, riskResult, impactResult, null, null);
    }

    public GovernanceDecision decide(
            CompatibilityResult compatibilityResult,
            DiffResult diffResult,
            RiskResult riskResult,
            ImpactResult impactResult,
            String oldSchemaName,
            String newSchemaName
    ) {
        if (schemaNameChanged(oldSchemaName, newSchemaName)) {
            return GovernanceDecision.SUGGEST_NEW_SUBJECT;
        }

        boolean breaking = isBreaking(compatibilityResult, diffResult, riskResult);
        int activeConsumers = impactResult != null ? impactResult.affectedConsumersCount() : 0;
        boolean hasCriticalServices = impactResult != null && !impactResult.criticalServices().isEmpty();

        if (breaking && hasCriticalServices) {
            return GovernanceDecision.REJECT;
        }

        if (breaking && activeConsumers > 0) {
            return GovernanceDecision.REQUIRE_CONSUMER_UPGRADE_FIRST;
        }

        if (riskResult != null && riskResult.getRiskLevel() == RiskLevel.HIGH) {
            return GovernanceDecision.REJECT;
        }

        if (riskResult != null && riskResult.getRiskLevel() == RiskLevel.MEDIUM) {
            return GovernanceDecision.ALLOW_WITH_CAUTION;
        }

        if (compatibilityResult != null && compatibilityResult.isCompatible() && activeConsumers > 0) {
            return GovernanceDecision.ALLOW_WITH_CAUTION;
        }

        return GovernanceDecision.ALLOW;
    }

    public List<String> explain(
            GovernanceDecision decision,
            CompatibilityResult compatibilityResult,
            DiffResult diffResult,
            RiskResult riskResult,
            ImpactResult impactResult
    ) {
        return explain(decision, compatibilityResult, diffResult, riskResult, impactResult, null, null);
    }

    public List<String> explain(
            GovernanceDecision decision,
            CompatibilityResult compatibilityResult,
            DiffResult diffResult,
            RiskResult riskResult,
            ImpactResult impactResult,
            String oldSchemaName,
            String newSchemaName
    ) {
        List<String> explanation = new ArrayList<>();

        if (schemaNameChanged(oldSchemaName, newSchemaName)) {
            explanation.add("Schema name changed");
        }

        boolean formallyIncompatible = compatibilityResult != null && !compatibilityResult.isCompatible();
        if (formallyIncompatible) {
            explanation.add("Schema change is formally incompatible");
        }

        boolean breaking = isBreaking(compatibilityResult, diffResult, riskResult);
        if (breaking && impactResult != null && impactResult.affectedConsumersCount() > 0) {
            explanation.add("Breaking change affects active consumers");
        } else if (breaking) {
            explanation.add("Breaking change detected");
        }

        if (impactResult != null && !impactResult.criticalServices().isEmpty()) {
            explanation.add("Critical services are affected");
        }

        if (riskResult != null && riskResult.getRiskLevel() != null) {
            explanation.add("Risk level is " + riskResult.getRiskLevel().name());
        }

        if (decision == GovernanceDecision.ALLOW_WITH_CAUTION
                || decision == GovernanceDecision.REQUIRE_CONSUMER_UPGRADE_FIRST
                || decision == GovernanceDecision.REJECT) {
            explanation.add("Manual review is required before schema promotion");
        }

        if (explanation.isEmpty()) {
            explanation.add("No governance blockers detected");
        }

        return explanation;
    }

    private boolean schemaNameChanged(String oldSchemaName, String newSchemaName) {
        return oldSchemaName != null
                && newSchemaName != null
                && !Objects.equals(oldSchemaName, newSchemaName);
    }

    private boolean isBreaking(
            CompatibilityResult compatibilityResult,
            DiffResult diffResult,
            RiskResult riskResult
    ) {
        if (compatibilityResult != null && !compatibilityResult.isCompatible()) {
            return true;
        }

        if (compatibilityResult != null && compatibilityResult.getIssues().stream().anyMatch(Issue::isBreakingChange)) {
            return true;
        }

        if (diffResult != null && diffResult.getChanges().stream().anyMatch(change ->
                change.getChangeType() == FieldChangeType.REMOVED
                        || change.getChangeType() == FieldChangeType.TYPE_CHANGED
                        || change.getChangeType() == FieldChangeType.REQUIRED_ADDED
                        || change.getChangeType() == FieldChangeType.OPTIONAL_BECAME_REQUIRED
        )) {
            return true;
        }

        return riskResult != null
                && riskResult.getRiskLevel() == RiskLevel.HIGH
                && riskResult.getRiskFactors().stream().anyMatch(this::isBreakingRiskFactor);
    }

    private boolean isBreakingRiskFactor(RiskFactor riskFactor) {
        return switch (riskFactor.code()) {
            case "COMPATIBILITY_BREAKING_CHANGE",
                 "DIFF_FIELD_REMOVED",
                 "DIFF_FIELD_TYPE_CHANGED",
                 "DIFF_REQUIRED_FIELD_ADDED",
                 "DIFF_OPTIONAL_BECAME_REQUIRED",
                 "IMPACT_BREAKING_WITH_CONSUMERS" -> true;
            default -> false;
        };
    }
}
