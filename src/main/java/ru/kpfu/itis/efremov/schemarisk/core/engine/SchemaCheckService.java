package ru.kpfu.itis.efremov.schemarisk.core.engine;

import org.springframework.stereotype.Service;
import ru.kpfu.itis.efremov.schemarisk.api.dto.SchemaCheckRequest;
import ru.kpfu.itis.efremov.schemarisk.core.diff.AvroDiffService;
import ru.kpfu.itis.efremov.schemarisk.core.diff.DiffResult;
import ru.kpfu.itis.efremov.schemarisk.core.risk.RiskResult;
import ru.kpfu.itis.efremov.schemarisk.core.risk.RiskScorer;
import ru.kpfu.itis.efremov.schemarisk.core.recommend.RecommendationService;
import ru.kpfu.itis.efremov.schemarisk.model.CompatibilityMode;
import ru.kpfu.itis.efremov.schemarisk.model.SchemaType;
import ru.kpfu.itis.efremov.schemarisk.schema.ParsedSchema;
import ru.kpfu.itis.efremov.schemarisk.schema.SchemaProvider;
import ru.kpfu.itis.efremov.schemarisk.schema.SchemaProviderRegistry;
import ru.kpfu.itis.efremov.schemarisk.schema.avro.AvroParsedSchema;

import java.util.List;

@Service
public class SchemaCheckService {

    private final SchemaProviderRegistry providerRegistry;
    private final CompatibilityEngine compatibilityEngine;
    private final AvroDiffService avroDiffService;
    private final RiskScorer riskScorer;
    private final RecommendationService recommendationService;

    public SchemaCheckService(SchemaProviderRegistry providerRegistry,
                              CompatibilityEngine compatibilityEngine,
                              AvroDiffService avroDiffService,
                              RiskScorer riskScorer,
                              RecommendationService recommendationService) {
        this.providerRegistry = providerRegistry;
        this.compatibilityEngine = compatibilityEngine;
        this.avroDiffService = avroDiffService;
        this.riskScorer = riskScorer;
        this.recommendationService = recommendationService;
    }

    public ResultWithAll checkSchemas(SchemaCheckRequest request) {
        SchemaProvider provider = providerRegistry.getProvider(request.getSchemaType());

        ParsedSchema oldSchema = provider.parseSchema(request.getOldSchema());
        ParsedSchema newSchema = provider.parseSchema(request.getNewSchema());

        CompatibilityMode mode = request.getCompatibilityMode();
        if (mode == null) {
            mode = CompatibilityMode.BACKWARD;
        }

        DiffResult diff = null;
        if (request.getSchemaType() == SchemaType.AVRO) {
            diff = avroDiffService.diff(
                    (AvroParsedSchema) oldSchema,
                    (AvroParsedSchema) newSchema
            );
        }

        CompatibilityResult compatibilityResult = compatibilityEngine.check(oldSchema, newSchema, mode);

        RiskResult riskResult = riskScorer.score(
                compatibilityResult,
                diff != null ? diff.getChanges() : List.of()
        );

        List<String> recommendations = recommendationService.generateRecommendations(
                compatibilityResult,
                diff,
                riskResult
        );

        return new ResultWithAll(compatibilityResult, diff, riskResult, recommendations);
    }

    public record ResultWithAll(
            CompatibilityResult compatibilityResult,
            DiffResult diffResult,
            RiskResult riskResult,
            List<String> recommendations
    ) {}
}
