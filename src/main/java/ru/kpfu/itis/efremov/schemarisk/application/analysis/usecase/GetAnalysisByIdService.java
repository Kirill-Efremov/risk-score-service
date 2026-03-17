package ru.kpfu.itis.efremov.schemarisk.application.analysis.usecase;

import org.springframework.stereotype.Service;
import ru.kpfu.itis.efremov.schemarisk.application.analysis.model.AnalysisRecord;
import ru.kpfu.itis.efremov.schemarisk.application.port.AnalysisRepository;

@Service
public class GetAnalysisByIdService {

    private final AnalysisRepository analysisRepository;

    public GetAnalysisByIdService(AnalysisRepository analysisRepository) {
        this.analysisRepository = analysisRepository;
    }

    public AnalysisRecord getById(Long analysisId) {
        return analysisRepository.getById(analysisId);
    }
}
