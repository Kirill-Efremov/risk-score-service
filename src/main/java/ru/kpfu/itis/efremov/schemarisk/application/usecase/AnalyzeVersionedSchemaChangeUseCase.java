package ru.kpfu.itis.efremov.schemarisk.application.usecase;

import org.springframework.stereotype.Service;

@Service
public class AnalyzeVersionedSchemaChangeUseCase {

    private final VersionedSchemaChangeResolver versionedSchemaChangeResolver;
    private final AnalyzeSchemaChangeUseCase analyzeSchemaChangeUseCase;

    public AnalyzeVersionedSchemaChangeUseCase(
            VersionedSchemaChangeResolver versionedSchemaChangeResolver,
            AnalyzeSchemaChangeUseCase analyzeSchemaChangeUseCase
    ) {
        this.versionedSchemaChangeResolver = versionedSchemaChangeResolver;
        this.analyzeSchemaChangeUseCase = analyzeSchemaChangeUseCase;
    }

    public AnalyzeSchemaChangeResult analyze(AnalyzeVersionedSchemaChangeCommand command) {
        ResolvedVersionedSchemaChange resolvedChange = versionedSchemaChangeResolver.resolve(command);

        return analyzeSchemaChangeUseCase.analyze(
                new AnalyzeSchemaChangeCommand(
                        resolvedChange.schemaType(),
                        command.compatibilityMode(),
                        resolvedChange.oldSchema(),
                        resolvedChange.newSchema()
                )
        );
    }
}
