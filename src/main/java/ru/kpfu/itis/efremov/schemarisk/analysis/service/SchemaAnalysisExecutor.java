package ru.kpfu.itis.efremov.schemarisk.analysis.service;

import org.springframework.stereotype.Service;
import ru.kpfu.itis.efremov.schemarisk.analysis.model.SchemaAnalysisResult;
import ru.kpfu.itis.efremov.schemarisk.analysis.model.SchemaAnalysisInput;
import ru.kpfu.itis.efremov.schemarisk.analysis.governance.RecommendationService;
import ru.kpfu.itis.efremov.schemarisk.analysis.diff.AvroDiffService;
import ru.kpfu.itis.efremov.schemarisk.analysis.diff.DiffResult;
import ru.kpfu.itis.efremov.schemarisk.analysis.compatibility.CompatibilityEngine;
import ru.kpfu.itis.efremov.schemarisk.analysis.compatibility.CompatibilityResult;
import ru.kpfu.itis.efremov.schemarisk.analysis.risk.RiskResult;
import ru.kpfu.itis.efremov.schemarisk.analysis.risk.RiskScorer;
import ru.kpfu.itis.efremov.schemarisk.analysis.compatibility.ParsedSchema;
import ru.kpfu.itis.efremov.schemarisk.analysis.compatibility.SchemaProvider;
import ru.kpfu.itis.efremov.schemarisk.analysis.compatibility.SchemaProviderRegistry;
import ru.kpfu.itis.efremov.schemarisk.analysis.compatibility.AvroParsedSchema;
import ru.kpfu.itis.efremov.schemarisk.common.model.CompatibilityMode;
import ru.kpfu.itis.efremov.schemarisk.common.model.SchemaType;

import java.util.List;

@Service
public class SchemaAnalysisExecutor {

    private final SchemaProviderRegistry providerRegistry;
    private final CompatibilityEngine compatibilityEngine;
    private final AvroDiffService avroDiffService;
    private final RiskScorer riskScorer;
    private final RecommendationService recommendationService;

    public SchemaAnalysisExecutor(
            SchemaProviderRegistry providerRegistry,
            CompatibilityEngine compatibilityEngine,
            AvroDiffService avroDiffService,
            RiskScorer riskScorer,
            RecommendationService recommendationService
    ) {
        this.providerRegistry = providerRegistry;
        this.compatibilityEngine = compatibilityEngine;
        this.avroDiffService = avroDiffService;
        this.riskScorer = riskScorer;
        this.recommendationService = recommendationService;
    }

    public SchemaAnalysisResult execute(SchemaAnalysisInput input) {
        SchemaProvider provider = providerRegistry.getProvider(input.schemaType());

        ParsedSchema oldSchema = provider.parseSchema(input.previousSchema());
        ParsedSchema newSchema = provider.parseSchema(input.candidateSchema());

        CompatibilityMode compatibilityMode = resolveCompatibilityMode(input.compatibilityMode());
        DiffResult diffResult = buildDiffIfSupported(input.schemaType(), oldSchema, newSchema);
        CompatibilityResult compatibilityResult = compatibilityEngine.check(oldSchema, newSchema, compatibilityMode);
        RiskResult riskResult = riskScorer.score(
                compatibilityResult,
                diffResult != null ? diffResult.getChanges() : List.of()
        );
        List<String> recommendations = recommendationService.generateRecommendations(
                compatibilityResult,
                diffResult,
                riskResult
        );

        return new SchemaAnalysisResult(
                compatibilityResult,
                diffResult,
                riskResult,
                recommendations,
                null,
                null,
                null,
                List.of()
        );
    }

    private CompatibilityMode resolveCompatibilityMode(CompatibilityMode compatibilityMode) {
        return compatibilityMode != null ? compatibilityMode : CompatibilityMode.BACKWARD;
    }

    private DiffResult buildDiffIfSupported(
            SchemaType schemaType,
            ParsedSchema oldSchema,
            ParsedSchema newSchema
    ) {
        if (schemaType != SchemaType.AVRO) {
            return null;
        }

        return avroDiffService.diff(
                (AvroParsedSchema) oldSchema,
                (AvroParsedSchema) newSchema
        );
    }
}





