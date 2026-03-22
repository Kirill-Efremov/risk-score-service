package ru.kpfu.itis.efremov.schemarisk.application.usecase;

import org.springframework.stereotype.Service;
import ru.kpfu.itis.efremov.schemarisk.application.impact.model.ImpactResult;
import ru.kpfu.itis.efremov.schemarisk.application.impact.service.ImpactAnalysisService;
import ru.kpfu.itis.efremov.schemarisk.application.analysis.model.SaveAnalysisCommand;
import ru.kpfu.itis.efremov.schemarisk.application.port.AnalysisRepository;
import ru.kpfu.itis.efremov.schemarisk.core.risk.RiskLevel;
import ru.kpfu.itis.efremov.schemarisk.core.risk.RiskResult;
import ru.kpfu.itis.efremov.schemarisk.model.Decision;

@Service
public class AnalyzeVersionedSchemaChangeService {

    private final VersionedSchemaChangeResolver versionedSchemaChangeResolver;
    private final SchemaAnalysisExecutor schemaAnalysisExecutor;
    private final AnalysisRepository analysisRepository;
    private final ImpactAnalysisService impactAnalysisService;

    public AnalyzeVersionedSchemaChangeService(
            VersionedSchemaChangeResolver versionedSchemaChangeResolver,
            SchemaAnalysisExecutor schemaAnalysisExecutor,
            AnalysisRepository analysisRepository,
            ImpactAnalysisService impactAnalysisService
    ) {
        this.versionedSchemaChangeResolver = versionedSchemaChangeResolver;
        this.schemaAnalysisExecutor = schemaAnalysisExecutor;
        this.analysisRepository = analysisRepository;
        this.impactAnalysisService = impactAnalysisService;
    }

    public AnalyzeSchemaChangeResult analyze(AnalyzeVersionedSchemaChangeCommand command) {
        ResolvedVersionedSchemaChange resolvedChange = versionedSchemaChangeResolver.resolve(command);
        AnalyzeSchemaChangeResult baseResult = schemaAnalysisExecutor.execute(
                new AnalyzeSchemaChangeCommand(
                        resolvedChange.schemaType(),
                        command.compatibilityMode(),
                        resolvedChange.oldSchema(),
                        resolvedChange.newSchema()
                )
        );
        ImpactResult impact = impactAnalysisService.analyze(
                resolvedChange.oldSchemaVersion().subject().name(),
                resolvedChange.oldSchemaVersion().version(),
                resolvedChange.newSchemaVersion().version(),
                baseResult.compatibilityResult()
        );
        RiskResult adjustedRisk = applyImpactToRisk(baseResult.riskResult(), impact);
        AnalyzeSchemaChangeResult result = new AnalyzeSchemaChangeResult(
                baseResult.compatibilityResult(),
                baseResult.diffResult(),
                adjustedRisk,
                baseResult.recommendations(),
                impact
        );

        analysisRepository.save(
                new SaveAnalysisCommand(
                        resolvedChange.oldSchemaVersion().subject().id(),
                        resolvedChange.oldSchemaVersion().subject().name(),
                        resolvedChange.oldSchemaVersion().id(),
                        resolvedChange.oldSchemaVersion().version(),
                        resolvedChange.newSchemaVersion().id(),
                        resolvedChange.newSchemaVersion().version(),
                        result.compatibilityResult(),
                        result.diffResult(),
                        result.riskResult(),
                        result.recommendations(),
                        result.impact(),
                        null
                )
        );

        return result;
    }

    private RiskResult applyImpactToRisk(RiskResult baseRisk, ImpactResult impact) {
        int adjustedScore = baseRisk.getRiskScore();

        if (impact.breaking() && impact.affectedConsumersCount() > 0) {
            adjustedScore += impact.affectedConsumersCount() * 5;
        }
        if (!impact.criticalServices().isEmpty()) {
            adjustedScore += 20;
        }

        adjustedScore = Math.min(Math.max(adjustedScore, 0), 100);

        RiskLevel level;
        Decision decision;
        if (adjustedScore >= 70) {
            level = RiskLevel.HIGH;
            decision = Decision.BLOCK;
        } else if (adjustedScore >= 30) {
            level = RiskLevel.MEDIUM;
            decision = Decision.WARN;
        } else {
            level = RiskLevel.LOW;
            decision = Decision.ALLOW;
        }

        return RiskResult.builder()
                .riskScore(adjustedScore)
                .riskLevel(level)
                .decision(decision)
                .build();
    }
}
