package ru.kpfu.itis.efremov.schemarisk.analysis.service;

import org.springframework.stereotype.Service;
import ru.kpfu.itis.efremov.schemarisk.analysis.model.AnalyzeSchemaChangeCommand;
import ru.kpfu.itis.efremov.schemarisk.analysis.model.AnalyzeSchemaChangeResult;
import ru.kpfu.itis.efremov.schemarisk.analysis.model.AnalyzeVersionedSchemaChangeCommand;
import ru.kpfu.itis.efremov.schemarisk.analysis.model.ResolvedVersionedSchemaChange;
import ru.kpfu.itis.efremov.schemarisk.analysis.impact.ImpactResult;
import ru.kpfu.itis.efremov.schemarisk.analysis.impact.ImpactAnalysisService;
import ru.kpfu.itis.efremov.schemarisk.history.model.SaveAnalysisCommand;
import ru.kpfu.itis.efremov.schemarisk.common.port.AnalysisRepository;
import ru.kpfu.itis.efremov.schemarisk.analysis.governance.GovernanceDecision;
import ru.kpfu.itis.efremov.schemarisk.analysis.governance.RecommendationService;
import ru.kpfu.itis.efremov.schemarisk.analysis.risk.RiskLevel;
import ru.kpfu.itis.efremov.schemarisk.analysis.risk.RiskResult;
import ru.kpfu.itis.efremov.schemarisk.analysis.compatibility.ParsedSchema;
import ru.kpfu.itis.efremov.schemarisk.analysis.compatibility.SchemaProvider;
import ru.kpfu.itis.efremov.schemarisk.analysis.compatibility.SchemaProviderRegistry;
import ru.kpfu.itis.efremov.schemarisk.analysis.compatibility.AvroParsedSchema;
import ru.kpfu.itis.efremov.schemarisk.common.model.Decision;

import java.util.List;

@Service
public class AnalyzeVersionedSchemaChangeService {

    private final VersionedSchemaChangeResolver versionedSchemaChangeResolver;
    private final SchemaAnalysisExecutor schemaAnalysisExecutor;
    private final AnalysisRepository analysisRepository;
    private final ImpactAnalysisService impactAnalysisService;
    private final RecommendationService recommendationService;
    private final SchemaProviderRegistry schemaProviderRegistry;

    public AnalyzeVersionedSchemaChangeService(
            VersionedSchemaChangeResolver versionedSchemaChangeResolver,
            SchemaAnalysisExecutor schemaAnalysisExecutor,
            AnalysisRepository analysisRepository,
            ImpactAnalysisService impactAnalysisService,
            RecommendationService recommendationService,
            SchemaProviderRegistry schemaProviderRegistry
    ) {
        this.versionedSchemaChangeResolver = versionedSchemaChangeResolver;
        this.schemaAnalysisExecutor = schemaAnalysisExecutor;
        this.analysisRepository = analysisRepository;
        this.impactAnalysisService = impactAnalysisService;
        this.recommendationService = recommendationService;
        this.schemaProviderRegistry = schemaProviderRegistry;
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
        String oldSchemaName = extractSchemaName(resolvedChange.schemaType(), resolvedChange.oldSchema());
        String newSchemaName = extractSchemaName(resolvedChange.schemaType(), resolvedChange.newSchema());
        GovernanceDecision governanceDecision = recommendationService.decide(
                baseResult.compatibilityResult().isCompatible(),
                !baseResult.compatibilityResult().isCompatible(),
                impact.affectedConsumersCount(),
                impact.affectedProducersCount(),
                !impact.criticalServices().isEmpty(),
                oldSchemaName,
                newSchemaName
        );
        List<String> decisionExplanation = recommendationService.buildExplanation(
                governanceDecision,
                !baseResult.compatibilityResult().isCompatible(),
                impact.affectedConsumersCount(),
                !impact.criticalServices().isEmpty()
        );
        AnalyzeSchemaChangeResult result = new AnalyzeSchemaChangeResult(
                baseResult.compatibilityResult(),
                baseResult.diffResult(),
                adjustedRisk,
                baseResult.recommendations(),
                impact,
                governanceDecision,
                decisionExplanation
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

    private String extractSchemaName(ru.kpfu.itis.efremov.schemarisk.common.model.SchemaType schemaType, String schemaText) {
        SchemaProvider provider = schemaProviderRegistry.getProvider(schemaType);
        ParsedSchema parsedSchema = provider.parseSchema(schemaText);
        if (parsedSchema instanceof AvroParsedSchema avroParsedSchema) {
            return avroParsedSchema.getAvroSchema().getFullName();
        }
        return null;
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




