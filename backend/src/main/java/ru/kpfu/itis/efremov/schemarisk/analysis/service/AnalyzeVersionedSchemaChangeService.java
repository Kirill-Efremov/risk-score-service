package ru.kpfu.itis.efremov.schemarisk.analysis.service;

import org.springframework.stereotype.Service;
import ru.kpfu.itis.efremov.schemarisk.analysis.graph.UsageGraphService;
import ru.kpfu.itis.efremov.schemarisk.analysis.graph.dto.UsageGraphResponse;
import ru.kpfu.itis.efremov.schemarisk.analysis.model.SchemaAnalysisResult;
import ru.kpfu.itis.efremov.schemarisk.analysis.model.AnalyzeVersionedSchemaChangeCommand;
import ru.kpfu.itis.efremov.schemarisk.analysis.model.ResolvedVersionedSchemaChange;
import ru.kpfu.itis.efremov.schemarisk.analysis.model.SchemaAnalysisInput;
import ru.kpfu.itis.efremov.schemarisk.analysis.impact.ImpactResult;
import ru.kpfu.itis.efremov.schemarisk.analysis.impact.ImpactAnalysisService;
import ru.kpfu.itis.efremov.schemarisk.history.model.SaveAnalysisCommand;
import ru.kpfu.itis.efremov.schemarisk.common.port.AnalysisRepository;
import ru.kpfu.itis.efremov.schemarisk.analysis.governance.GovernanceDecision;
import ru.kpfu.itis.efremov.schemarisk.analysis.governance.GovernanceDecisionService;
import ru.kpfu.itis.efremov.schemarisk.analysis.governance.RecommendationService;
import ru.kpfu.itis.efremov.schemarisk.analysis.governance.SchemaPromotionStatus;
import ru.kpfu.itis.efremov.schemarisk.analysis.governance.StructuredRecommendation;
import ru.kpfu.itis.efremov.schemarisk.analysis.risk.RiskResult;
import ru.kpfu.itis.efremov.schemarisk.analysis.risk.RiskScorer;
import ru.kpfu.itis.efremov.schemarisk.analysis.compatibility.ParsedSchema;
import ru.kpfu.itis.efremov.schemarisk.analysis.compatibility.SchemaProvider;
import ru.kpfu.itis.efremov.schemarisk.analysis.compatibility.SchemaProviderRegistry;
import ru.kpfu.itis.efremov.schemarisk.analysis.compatibility.AvroParsedSchema;
import ru.kpfu.itis.efremov.schemarisk.auth.service.CurrentUserService;

import java.util.List;

@Service
public class AnalyzeVersionedSchemaChangeService {

    private final VersionedSchemaChangeResolver versionedSchemaChangeResolver;
    private final SchemaAnalysisExecutor schemaAnalysisExecutor;
    private final AnalysisRepository analysisRepository;
    private final ImpactAnalysisService impactAnalysisService;
    private final UsageGraphService usageGraphService;
    private final GovernanceDecisionService governanceDecisionService;
    private final RecommendationService recommendationService;
    private final SchemaProviderRegistry schemaProviderRegistry;
    private final RiskScorer riskScorer;
    private final CurrentUserService currentUserService;

    public AnalyzeVersionedSchemaChangeService(
            VersionedSchemaChangeResolver versionedSchemaChangeResolver,
            SchemaAnalysisExecutor schemaAnalysisExecutor,
            AnalysisRepository analysisRepository,
            ImpactAnalysisService impactAnalysisService,
            UsageGraphService usageGraphService,
            GovernanceDecisionService governanceDecisionService,
            RecommendationService recommendationService,
            SchemaProviderRegistry schemaProviderRegistry,
            RiskScorer riskScorer,
            CurrentUserService currentUserService
    ) {
        this.versionedSchemaChangeResolver = versionedSchemaChangeResolver;
        this.schemaAnalysisExecutor = schemaAnalysisExecutor;
        this.analysisRepository = analysisRepository;
        this.impactAnalysisService = impactAnalysisService;
        this.usageGraphService = usageGraphService;
        this.governanceDecisionService = governanceDecisionService;
        this.recommendationService = recommendationService;
        this.schemaProviderRegistry = schemaProviderRegistry;
        this.riskScorer = riskScorer;
        this.currentUserService = currentUserService;
    }

    public SchemaAnalysisResult analyze(AnalyzeVersionedSchemaChangeCommand command) {
        String resolvedCreatedBy = resolveUsername(command.createdBy());
        ResolvedVersionedSchemaChange resolvedChange = versionedSchemaChangeResolver.resolve(command);
        SchemaAnalysisResult baseResult = schemaAnalysisExecutor.execute(
                new SchemaAnalysisInput(
                        resolvedChange.schemaType(),
                        command.compatibilityMode(),
                        resolvedChange.oldSchema(),
                        resolvedChange.newSchema()
                )
        );
        ImpactResult impact = impactAnalysisService.analyze(
                resolvedChange.subject(),
                resolvedChange.oldSchemaVersion().version(),
                resolvedChange.newSchemaVersion().version(),
                baseResult.compatibilityResult()
        );
        UsageGraphResponse impactGraph = usageGraphService.buildGraph(
                resolvedChange.subject(),
                impact
        );
        RiskResult adjustedRisk = riskScorer.score(
                baseResult.compatibilityResult(),
                baseResult.diffResult() != null ? baseResult.diffResult().getChanges() : List.of(),
                impact
        );
        String oldSchemaName = extractSchemaName(resolvedChange.schemaType(), resolvedChange.oldSchema());
        String newSchemaName = extractSchemaName(resolvedChange.schemaType(), resolvedChange.newSchema());
        GovernanceDecision governanceDecision = governanceDecisionService.decide(
                baseResult.compatibilityResult(),
                baseResult.diffResult(),
                adjustedRisk,
                impact,
                oldSchemaName,
                newSchemaName
        );
        List<String> decisionExplanation = governanceDecisionService.explain(
                governanceDecision,
                baseResult.compatibilityResult(),
                baseResult.diffResult(),
                adjustedRisk,
                impact,
                oldSchemaName,
                newSchemaName
        );
        List<StructuredRecommendation> structuredRecommendations = recommendationService.generateStructuredRecommendations(
                baseResult.compatibilityResult(),
                baseResult.diffResult(),
                adjustedRisk,
                impact,
                governanceDecision
        );
        var savedRecord = analysisRepository.save(
                new SaveAnalysisCommand(
                        resolvedChange.oldSchemaVersion().subject().localId(),
                        resolvedChange.subject(),
                        resolvedChange.oldSchemaVersion().localId(),
                        resolvedChange.oldSchemaVersion().version(),
                        resolvedChange.newSchemaVersion().localId(),
                        resolvedChange.newSchemaVersion().version(),
                        resolvedChange.newSchemaVersion().sourceType(),
                        resolvedChange.newSchemaVersion().externalSchemaId(),
                        baseResult.compatibilityResult(),
                        baseResult.diffResult(),
                        adjustedRisk,
                        governanceDecision,
                        decisionExplanation,
                        baseResult.recommendations(),
                        structuredRecommendations,
                        impact,
                        impactGraph,
                        resolvedChange.oldSchema(),
                        resolvedChange.newSchema(),
                        resolvedCreatedBy,
                        command.promotionAttempted(),
                        null,
                        command.promotionAttempted() ? SchemaPromotionStatus.ANALYSIS_ONLY : null,
                        null,
                        null
                )
        );

        return new SchemaAnalysisResult(
                baseResult.compatibilityResult(),
                baseResult.diffResult(),
                adjustedRisk,
                baseResult.recommendations(),
                structuredRecommendations,
                impact,
                impactGraph,
                resolvedChange.oldSchema(),
                resolvedChange.newSchema(),
                governanceDecision,
                decisionExplanation,
                savedRecord.id()
        );
    }

    private String extractSchemaName(ru.kpfu.itis.efremov.schemarisk.common.model.SchemaType schemaType, String schemaText) {
        SchemaProvider provider = schemaProviderRegistry.getProvider(schemaType);
        ParsedSchema parsedSchema = provider.parseSchema(schemaText);
        if (parsedSchema instanceof AvroParsedSchema avroParsedSchema) {
            return avroParsedSchema.getAvroSchema().getFullName();
        }
        return null;
    }

    private String resolveUsername(String fallbackUsername) {
        return currentUserService.currentUsernameOptional()
                .orElseGet(() -> normalizeFallbackUsername(fallbackUsername));
    }

    private String normalizeFallbackUsername(String fallbackUsername) {
        if (fallbackUsername == null || fallbackUsername.isBlank()) {
            return "system";
        }
        return fallbackUsername.trim();
    }
}

