package ru.kpfu.itis.efremov.schemarisk.application.analysis.usecase;

import org.springframework.stereotype.Service;
import ru.kpfu.itis.efremov.schemarisk.application.analysis.model.AnalysisRecord;
import ru.kpfu.itis.efremov.schemarisk.application.port.AnalysisRepository;

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
