package ru.kpfu.itis.efremov.schemarisk.infrastructure.analysis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import ru.kpfu.itis.efremov.schemarisk.application.impact.model.ImpactResult;
import ru.kpfu.itis.efremov.schemarisk.core.diff.DiffResult;
import ru.kpfu.itis.efremov.schemarisk.model.Issue;

import java.util.List;

@Component
public class AnalysisJsonMapper {

    private final ObjectMapper objectMapper;

    public AnalysisJsonMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String writeIssues(List<Issue> issues) {
        return writeValue(issues);
    }

    public List<Issue> readIssues(String json) {
        return readValue(json, new TypeReference<>() {});
    }

    public String writeDiff(DiffResult diffResult) {
        return diffResult == null ? null : writeValue(diffResult);
    }

    public DiffResult readDiff(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        return readValue(json, new TypeReference<>() {});
    }

    public String writeRecommendations(List<String> recommendations) {
        return writeValue(recommendations);
    }

    public List<String> readRecommendations(String json) {
        return readValue(json, new TypeReference<>() {});
    }

    public String writeImpact(ImpactResult impactResult) {
        return impactResult == null ? null : writeValue(impactResult);
    }

    public ImpactResult readImpact(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        return readValue(json, new TypeReference<>() {});
    }

    private String writeValue(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize analysis payload", exception);
        }
    }

    private <T> T readValue(String json, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to deserialize analysis payload", exception);
        }
    }
}
