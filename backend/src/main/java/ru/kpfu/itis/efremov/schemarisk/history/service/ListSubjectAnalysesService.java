package ru.kpfu.itis.efremov.schemarisk.history.service;

import org.springframework.stereotype.Service;
import ru.kpfu.itis.efremov.schemarisk.history.model.AnalysisRecord;
import ru.kpfu.itis.efremov.schemarisk.common.port.AnalysisRepository;

import java.util.List;

@Service
public class ListSubjectAnalysesService {

    private final AnalysisRepository analysisRepository;

    public ListSubjectAnalysesService(AnalysisRepository analysisRepository) {
        this.analysisRepository = analysisRepository;
    }

    public List<AnalysisRecord> listBySubject(String subject) {
        return analysisRepository.listBySubject(subject);
    }
}




