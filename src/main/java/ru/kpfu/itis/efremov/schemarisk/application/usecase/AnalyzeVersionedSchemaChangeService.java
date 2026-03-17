package ru.kpfu.itis.efremov.schemarisk.application.usecase;

import org.springframework.stereotype.Service;
import ru.kpfu.itis.efremov.schemarisk.application.analysis.model.SaveAnalysisCommand;
import ru.kpfu.itis.efremov.schemarisk.application.port.AnalysisRepository;

@Service
public class AnalyzeVersionedSchemaChangeService {

    private final VersionedSchemaChangeResolver versionedSchemaChangeResolver;
    private final SchemaAnalysisExecutor schemaAnalysisExecutor;
    private final AnalysisRepository analysisRepository;

    public AnalyzeVersionedSchemaChangeService(
            VersionedSchemaChangeResolver versionedSchemaChangeResolver,
            SchemaAnalysisExecutor schemaAnalysisExecutor,
            AnalysisRepository analysisRepository
    ) {
        this.versionedSchemaChangeResolver = versionedSchemaChangeResolver;
        this.schemaAnalysisExecutor = schemaAnalysisExecutor;
        this.analysisRepository = analysisRepository;
    }

    public AnalyzeSchemaChangeResult analyze(AnalyzeVersionedSchemaChangeCommand command) {
        ResolvedVersionedSchemaChange resolvedChange = versionedSchemaChangeResolver.resolve(command);
        AnalyzeSchemaChangeResult result = schemaAnalysisExecutor.execute(
                new AnalyzeSchemaChangeCommand(
                        resolvedChange.schemaType(),
                        command.compatibilityMode(),
                        resolvedChange.oldSchema(),
                        resolvedChange.newSchema()
                )
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
                        null
                )
        );

        return result;
    }
}
