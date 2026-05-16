package ru.kpfu.itis.efremov.schemarisk.history.service;

import org.springframework.stereotype.Service;
import ru.kpfu.itis.efremov.schemarisk.history.model.AnalysisRecord;
import ru.kpfu.itis.efremov.schemarisk.common.port.AnalysisRepository;

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




