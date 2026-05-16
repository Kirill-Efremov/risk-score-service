package ru.kpfu.itis.efremov.schemarisk.analysis.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import ru.kpfu.itis.efremov.schemarisk.analysis.governance.GovernanceDecision;
import ru.kpfu.itis.efremov.schemarisk.analysis.governance.SchemaPromotionStatus;
import ru.kpfu.itis.efremov.schemarisk.analysis.model.AnalyzeVersionedSchemaChangeCommand;
import ru.kpfu.itis.efremov.schemarisk.analysis.model.SchemaAnalysisResult;
import ru.kpfu.itis.efremov.schemarisk.api.dto.SchemaAnalysisResponse;
import ru.kpfu.itis.efremov.schemarisk.api.dto.SchemaPromotionRequest;
import ru.kpfu.itis.efremov.schemarisk.api.dto.SchemaPromotionResponse;
import ru.kpfu.itis.efremov.schemarisk.catalog.model.RegisterSchemaVersionCommand;
import ru.kpfu.itis.efremov.schemarisk.catalog.model.SchemaSourceType;
import ru.kpfu.itis.efremov.schemarisk.catalog.model.SchemaVersionInfo;
import ru.kpfu.itis.efremov.schemarisk.catalog.model.SchemaVersionStatus;
import ru.kpfu.itis.efremov.schemarisk.catalog.exception.SchemaRegistryConflictException;
import ru.kpfu.itis.efremov.schemarisk.catalog.service.RegisterSchemaVersionService;
import ru.kpfu.itis.efremov.schemarisk.common.exception.ResourceNotFoundException;
import ru.kpfu.itis.efremov.schemarisk.common.model.CompatibilityMode;
import ru.kpfu.itis.efremov.schemarisk.common.port.AnalysisRepository;
import ru.kpfu.itis.efremov.schemarisk.common.port.SchemaCatalog;
import ru.kpfu.itis.efremov.schemarisk.history.model.UpdatePromotionMetadataCommand;

@Service
public class SchemaPromotionService {

    private final SchemaCatalog schemaCatalog;
    private final AnalyzeVersionedSchemaChangeService analyzeVersionedSchemaChangeService;
    private final RegisterSchemaVersionService registerSchemaVersionService;
    private final AnalysisRepository analysisRepository;

    public SchemaPromotionService(
            SchemaCatalog schemaCatalog,
            AnalyzeVersionedSchemaChangeService analyzeVersionedSchemaChangeService,
            RegisterSchemaVersionService registerSchemaVersionService,
            AnalysisRepository analysisRepository
    ) {
        this.schemaCatalog = schemaCatalog;
        this.analyzeVersionedSchemaChangeService = analyzeVersionedSchemaChangeService;
        this.registerSchemaVersionService = registerSchemaVersionService;
        this.analysisRepository = analysisRepository;
    }

    public SchemaPromotionResponse promote(String subject, SchemaPromotionRequest request) {
        SchemaVersionInfo latestVersion = findLatestVersion(subject);
        if (latestVersion == null) {
            return registerFirstVersion(subject, request);
        }

        SchemaAnalysisResult analysisResult = analyzeVersionedSchemaChangeService.analyze(
                new AnalyzeVersionedSchemaChangeCommand(
                        subject,
                        latestVersion.version(),
                        null,
                        request.getSchemaText(),
                        request.getSchemaType(),
                        request.getCompatibilityMode(),
                        request.getCreatedBy(),
                        true
                )
        );

        GovernanceDecision governanceDecision = analysisResult.governanceDecision();
        SchemaPromotionStatus promotionStatus = resolvePromotionStatus(governanceDecision);
        if (promotionStatus != SchemaPromotionStatus.REGISTERED) {
            updatePromotionMetadata(
                    analysisResult,
                    false,
                    promotionStatus,
                    null,
                    null
            );
            return new SchemaPromotionResponse(
                    subject,
                    false,
                    latestVersion.version(),
                    null,
                    null,
                    promotionStatus,
                    buildBlockedMessage(governanceDecision),
                    SchemaAnalysisResponse.fromResult(analysisResult),
                    latestVersion.schemaText(),
                    request.getSchemaText()
            );
        }

        try {
            SchemaVersionInfo registeredVersion = registerSchemaVersionService.register(
                    new RegisterSchemaVersionCommand(
                            subject,
                            request.getSchemaType(),
                            request.getCompatibilityMode() != null
                                    ? request.getCompatibilityMode()
                                    : CompatibilityMode.BACKWARD,
                            request.getDescription(),
                            request.getSchemaText(),
                            SchemaVersionStatus.ACTIVE,
                            SchemaSourceType.CONFLUENT,
                            null
                    )
            );
            updatePromotionMetadata(
                    analysisResult,
                    true,
                    SchemaPromotionStatus.REGISTERED,
                    registeredVersion.version(),
                    parseRegistryId(registeredVersion.externalSchemaId())
            );
            return new SchemaPromotionResponse(
                    subject,
                    true,
                    latestVersion.version(),
                    registeredVersion.version(),
                    parseRegistryId(registeredVersion.externalSchemaId()),
                    SchemaPromotionStatus.REGISTERED,
                    "Schema was registered in Schema Registry",
                    SchemaAnalysisResponse.fromResult(analysisResult),
                    latestVersion.schemaText(),
                    request.getSchemaText()
            );
        } catch (SchemaRegistryConflictException exception) {
            updatePromotionMetadata(
                    analysisResult,
                    false,
                    SchemaPromotionStatus.REGISTRY_REJECTED,
                    null,
                    null
            );
            return new SchemaPromotionResponse(
                    subject,
                    false,
                    latestVersion.version(),
                    null,
                    null,
                    SchemaPromotionStatus.REGISTRY_REJECTED,
                    buildRegistryRejectedMessage(exception),
                    SchemaAnalysisResponse.fromResult(analysisResult),
                    latestVersion.schemaText(),
                    request.getSchemaText()
            );
        } catch (RestClientResponseException exception) {
            updatePromotionMetadata(
                    analysisResult,
                    false,
                    SchemaPromotionStatus.REGISTRY_REJECTED,
                    null,
                    null
            );
            return new SchemaPromotionResponse(
                    subject,
                    false,
                    latestVersion.version(),
                    null,
                    null,
                    SchemaPromotionStatus.REGISTRY_REJECTED,
                    buildRegistryRejectedMessage(exception),
                    SchemaAnalysisResponse.fromResult(analysisResult),
                    latestVersion.schemaText(),
                    request.getSchemaText()
            );
        }
    }

    private SchemaVersionInfo findLatestVersion(String subject) {
        try {
            return schemaCatalog.getLatestVersion(subject);
        } catch (ResourceNotFoundException exception) {
            return null;
        }
    }

    private SchemaPromotionResponse registerFirstVersion(String subject, SchemaPromotionRequest request) {
        try {
            SchemaVersionInfo registeredVersion = registerSchemaVersionService.register(
                    new RegisterSchemaVersionCommand(
                            subject,
                            request.getSchemaType(),
                            request.getCompatibilityMode() != null
                                    ? request.getCompatibilityMode()
                                    : CompatibilityMode.BACKWARD,
                            request.getDescription(),
                            request.getSchemaText(),
                            SchemaVersionStatus.ACTIVE,
                            SchemaSourceType.CONFLUENT,
                            null
                    )
            );
            return new SchemaPromotionResponse(
                    subject,
                    true,
                    null,
                    registeredVersion.version(),
                    parseRegistryId(registeredVersion.externalSchemaId()),
                    SchemaPromotionStatus.REGISTERED,
                    "Schema was registered in Schema Registry as the first version",
                    null,
                    null,
                    request.getSchemaText()
            );
        } catch (SchemaRegistryConflictException exception) {
            return new SchemaPromotionResponse(
                    subject,
                    false,
                    null,
                    null,
                    null,
                    SchemaPromotionStatus.REGISTRY_REJECTED,
                    buildRegistryRejectedMessage(exception),
                    null,
                    null,
                    request.getSchemaText()
            );
        } catch (RestClientResponseException exception) {
            return new SchemaPromotionResponse(
                    subject,
                    false,
                    null,
                    null,
                    null,
                    SchemaPromotionStatus.REGISTRY_REJECTED,
                    buildRegistryRejectedMessage(exception),
                    null,
                    null,
                    request.getSchemaText()
            );
        }
    }

    private void updatePromotionMetadata(
            SchemaAnalysisResult analysisResult,
            Boolean registered,
            SchemaPromotionStatus registrationStatus,
            Integer registeredVersion,
            Integer schemaRegistryId
    ) {
        if (analysisResult == null || analysisResult.analysisId() == null) {
            return;
        }

        analysisRepository.updatePromotionMetadata(
                new UpdatePromotionMetadataCommand(
                        analysisResult.analysisId(),
                        true,
                        registered,
                        registrationStatus,
                        registeredVersion,
                        schemaRegistryId
                )
        );
    }

    private SchemaPromotionStatus resolvePromotionStatus(GovernanceDecision governanceDecision) {
        if (governanceDecision == null) {
            return SchemaPromotionStatus.ANALYSIS_ONLY;
        }

        return switch (governanceDecision) {
            case ALLOW -> SchemaPromotionStatus.REGISTERED;
            case ALLOW_WITH_CAUTION -> SchemaPromotionStatus.REQUIRES_MANUAL_APPROVAL;
            case REQUIRE_CONSUMER_UPGRADE_FIRST -> SchemaPromotionStatus.REQUIRES_CONSUMER_UPGRADE;
            case REJECT -> SchemaPromotionStatus.BLOCKED_BY_GOVERNANCE;
            case SUGGEST_NEW_SUBJECT -> SchemaPromotionStatus.SUGGEST_NEW_SUBJECT;
        };
    }

    private String buildBlockedMessage(GovernanceDecision governanceDecision) {
        if (governanceDecision == null) {
            return "Schema was not registered because governance decision is unavailable";
        }

        return switch (governanceDecision) {
            case ALLOW_WITH_CAUTION -> "Schema was not registered automatically because manual approval is required";
            case REQUIRE_CONSUMER_UPGRADE_FIRST ->
                    "Schema was not registered because consumers must be upgraded first";
            case REJECT -> "Schema was not registered because governance policy rejected the change";
            case SUGGEST_NEW_SUBJECT -> "Schema was not registered because governance suggests using a new subject";
            case ALLOW -> "Schema was not registered";
        };
    }

    private String buildRegistryRejectedMessage(RestClientResponseException exception) {
        String responseBody = exception.getResponseBodyAsString();
        if (responseBody != null && !responseBody.isBlank()) {
            return "Schema Registry rejected the schema: " + responseBody;
        }
        return "Schema Registry rejected the schema: " + exception.getStatusText();
    }

    private String buildRegistryRejectedMessage(SchemaRegistryConflictException exception) {
        if (exception.getRegistryResponseBody() != null && !exception.getRegistryResponseBody().isBlank()) {
            return "Schema Registry rejected the schema: " + exception.getRegistryResponseBody();
        }
        return "Schema Registry rejected the schema: " + exception.getMessage();
    }

    private Integer parseRegistryId(String externalSchemaId) {
        if (externalSchemaId == null || externalSchemaId.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(externalSchemaId);
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
