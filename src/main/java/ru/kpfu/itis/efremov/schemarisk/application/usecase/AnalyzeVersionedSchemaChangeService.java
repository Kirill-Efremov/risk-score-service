package ru.kpfu.itis.efremov.schemarisk.application.usecase;

import org.springframework.stereotype.Service;
import ru.kpfu.itis.efremov.schemarisk.application.impact.model.ImpactResult;
import ru.kpfu.itis.efremov.schemarisk.application.impact.service.ImpactAnalysisService;
import ru.kpfu.itis.efremov.schemarisk.application.analysis.model.SaveAnalysisCommand;
import ru.kpfu.itis.efremov.schemarisk.application.port.AnalysisRepository;
import ru.kpfu.itis.efremov.schemarisk.application.recommendation.model.GovernanceDecision;
import ru.kpfu.itis.efremov.schemarisk.application.recommendation.service.RecommendationService;
import ru.kpfu.itis.efremov.schemarisk.core.risk.RiskLevel;
import ru.kpfu.itis.efremov.schemarisk.core.risk.RiskResult;
import ru.kpfu.itis.efremov.schemarisk.infrastructure.schema.ParsedSchema;
import ru.kpfu.itis.efremov.schemarisk.infrastructure.schema.SchemaProvider;
import ru.kpfu.itis.efremov.schemarisk.infrastructure.schema.SchemaProviderRegistry;
import ru.kpfu.itis.efremov.schemarisk.infrastructure.schema.avro.AvroParsedSchema;
import ru.kpfu.itis.efremov.schemarisk.model.Decision;

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

    private String extractSchemaName(ru.kpfu.itis.efremov.schemarisk.model.SchemaType schemaType, String schemaText) {
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
