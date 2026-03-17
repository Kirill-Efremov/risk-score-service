package ru.kpfu.itis.efremov.schemarisk.application.port;

import ru.kpfu.itis.efremov.schemarisk.application.analysis.model.AnalysisRecord;
import ru.kpfu.itis.efremov.schemarisk.application.analysis.model.SaveAnalysisCommand;

import java.util.List;

public interface AnalysisRepository {

    AnalysisRecord save(SaveAnalysisCommand command);

    AnalysisRecord getById(Long analysisId);

    List<AnalysisRecord> listBySubject(String subject);
}
