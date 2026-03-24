package ru.kpfu.itis.efremov.schemarisk.analysis.service;

import org.springframework.stereotype.Service;
import ru.kpfu.itis.efremov.schemarisk.analysis.model.AnalyzeSchemaChangeCommand;
import ru.kpfu.itis.efremov.schemarisk.analysis.model.AnalyzeSchemaChangeResult;
import ru.kpfu.itis.efremov.schemarisk.history.model.SaveAnalysisCommand;
import ru.kpfu.itis.efremov.schemarisk.common.port.AnalysisRepository;

@Service
public class AnalyzeSchemaChangeService {

    private final SchemaAnalysisExecutor schemaAnalysisExecutor;
    private final AnalysisRepository analysisRepository;

    public AnalyzeSchemaChangeService(
            SchemaAnalysisExecutor schemaAnalysisExecutor,
            AnalysisRepository analysisRepository
    ) {
        this.schemaAnalysisExecutor = schemaAnalysisExecutor;
        this.analysisRepository = analysisRepository;
    }

    public AnalyzeSchemaChangeResult analyze(AnalyzeSchemaChangeCommand command) {
        AnalyzeSchemaChangeResult result = schemaAnalysisExecutor.execute(command);

        analysisRepository.save(
                new SaveAnalysisCommand(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
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
}




