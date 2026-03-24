package ru.kpfu.itis.efremov.schemarisk.common.port;

import ru.kpfu.itis.efremov.schemarisk.history.model.AnalysisRecord;
import ru.kpfu.itis.efremov.schemarisk.history.model.SaveAnalysisCommand;

import java.util.List;

public interface AnalysisRepository {

    AnalysisRecord save(SaveAnalysisCommand command);

    AnalysisRecord getById(Long analysisId);

    List<AnalysisRecord> listBySubject(String subject);
}




