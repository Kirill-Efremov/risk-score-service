package ru.kpfu.itis.efremov.schemarisk.history.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import ru.kpfu.itis.efremov.schemarisk.analysis.governance.StructuredRecommendation;
import ru.kpfu.itis.efremov.schemarisk.analysis.impact.ImpactResult;
import ru.kpfu.itis.efremov.schemarisk.analysis.diff.DiffResult;
import ru.kpfu.itis.efremov.schemarisk.analysis.risk.RiskFactor;
import ru.kpfu.itis.efremov.schemarisk.common.model.Issue;

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
        if (json == null || json.isBlank()) {
            return List.of();
        }
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

    public String writeRiskFactors(List<RiskFactor> riskFactors) {
        return writeValue(riskFactors);
    }

    public List<RiskFactor> readRiskFactors(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        return readValue(json, new TypeReference<>() {});
    }

    public String writeStructuredRecommendations(List<StructuredRecommendation> structuredRecommendations) {
        return writeValue(structuredRecommendations);
    }

    public List<StructuredRecommendation> readStructuredRecommendations(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        return readValue(json, new TypeReference<>() {});
    }

    public String writeDecisionExplanation(List<String> decisionExplanation) {
        return writeRecommendations(decisionExplanation);
    }

    public List<String> readDecisionExplanation(String json) {
        return readRecommendations(json);
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




