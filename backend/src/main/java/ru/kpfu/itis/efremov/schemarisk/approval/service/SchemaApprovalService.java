package ru.kpfu.itis.efremov.schemarisk.approval.service;

import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientResponseException;
import ru.kpfu.itis.efremov.schemarisk.analysis.governance.GovernanceDecision;
import ru.kpfu.itis.efremov.schemarisk.analysis.governance.SchemaPromotionStatus;
import ru.kpfu.itis.efremov.schemarisk.approval.dto.ApprovalDecisionRequest;
import ru.kpfu.itis.efremov.schemarisk.approval.dto.SchemaApprovalResponse;
import ru.kpfu.itis.efremov.schemarisk.approval.exception.ApprovalBaselineChangedException;
import ru.kpfu.itis.efremov.schemarisk.approval.exception.ApprovalNotAllowedException;
import ru.kpfu.itis.efremov.schemarisk.approval.exception.InvalidApprovalStateException;
import ru.kpfu.itis.efremov.schemarisk.approval.exception.SchemaApprovalNotFoundException;
import ru.kpfu.itis.efremov.schemarisk.approval.model.SchemaApprovalStatus;
import ru.kpfu.itis.efremov.schemarisk.approval.persistence.SchemaApprovalEntity;
import ru.kpfu.itis.efremov.schemarisk.approval.persistence.SchemaApprovalRepository;
import ru.kpfu.itis.efremov.schemarisk.auth.exception.AuthenticationRequiredException;
import ru.kpfu.itis.efremov.schemarisk.auth.service.CurrentUserService;
import ru.kpfu.itis.efremov.schemarisk.catalog.exception.SchemaRegistryConflictException;
import ru.kpfu.itis.efremov.schemarisk.catalog.model.RegisterSchemaVersionCommand;
import ru.kpfu.itis.efremov.schemarisk.catalog.model.SchemaSourceType;
import ru.kpfu.itis.efremov.schemarisk.catalog.model.SchemaVersionInfo;
import ru.kpfu.itis.efremov.schemarisk.catalog.model.SchemaVersionStatus;
import ru.kpfu.itis.efremov.schemarisk.catalog.service.RegisterSchemaVersionService;
import ru.kpfu.itis.efremov.schemarisk.common.exception.ResourceNotFoundException;
import ru.kpfu.itis.efremov.schemarisk.common.exception.SchemaRegistryUnavailableException;
import ru.kpfu.itis.efremov.schemarisk.common.model.CompatibilityMode;
import ru.kpfu.itis.efremov.schemarisk.common.model.SchemaType;
import ru.kpfu.itis.efremov.schemarisk.common.port.AnalysisRepository;
import ru.kpfu.itis.efremov.schemarisk.common.port.SchemaCatalog;
import ru.kpfu.itis.efremov.schemarisk.history.model.UpdatePromotionMetadataCommand;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Service
public class SchemaApprovalService {

    private static final int DEFAULT_USER_LIMIT = 50;
    private static final int DEFAULT_ADMIN_LIMIT = 100;
    private static final int MAX_LIMIT = 200;

    private final SchemaApprovalRepository schemaApprovalRepository;
    private final RegisterSchemaVersionService registerSchemaVersionService;
    private final SchemaCatalog schemaCatalog;
    private final CurrentUserService currentUserService;
    private final AnalysisRepository analysisRepository;

    public SchemaApprovalService(
            SchemaApprovalRepository schemaApprovalRepository,
            RegisterSchemaVersionService registerSchemaVersionService,
            SchemaCatalog schemaCatalog,
            CurrentUserService currentUserService,
            AnalysisRepository analysisRepository
    ) {
        this.schemaApprovalRepository = schemaApprovalRepository;
        this.registerSchemaVersionService = registerSchemaVersionService;
        this.schemaCatalog = schemaCatalog;
        this.currentUserService = currentUserService;
        this.analysisRepository = analysisRepository;
    }

    @Transactional
    public SchemaApprovalResponse createPendingApproval(
            String subject,
            String schemaType,
            String compatibilityMode,
            Integer oldVersion,
            String newSchemaText,
            Long analysisId,
            boolean formalCompatible,
            String governanceDecision,
            int riskScore,
            String riskLevel,
            String requestedBy
    ) {
        if (!formalCompatible) {
            throw new ApprovalNotAllowedException("Approval can only be created for formally compatible schema changes");
        }
        if (!GovernanceDecision.ALLOW_WITH_CAUTION.name().equals(governanceDecision)) {
            throw new ApprovalNotAllowedException("Approval can only be created for ALLOW_WITH_CAUTION governance decision");
        }

        Instant now = Instant.now();
        SchemaApprovalEntity entity = new SchemaApprovalEntity();
        entity.setSubject(subject);
        entity.setSchemaType(schemaType);
        entity.setCompatibilityMode(compatibilityMode);
        entity.setOldVersion(oldVersion);
        entity.setNewSchemaText(newSchemaText);
        entity.setAnalysisId(analysisId);
        entity.setFormalCompatible(true);
        entity.setGovernanceDecision(governanceDecision);
        entity.setRiskScore(riskScore);
        entity.setRiskLevel(riskLevel);
        entity.setStatus(SchemaApprovalStatus.PENDING);
        entity.setRequestedBy(normalizeUsername(requestedBy));
        entity.setRequestedAt(now);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        return SchemaApprovalResponse.fromEntity(schemaApprovalRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<SchemaApprovalResponse> listMyApprovals(String currentUser, SchemaApprovalStatus status, Integer limit) {
        String resolvedUser = requireUsername(currentUser);
        return schemaApprovalRepository.findAll(Sort.by(Sort.Direction.DESC, "requestedAt")).stream()
                .filter(approval -> resolvedUser.equalsIgnoreCase(approval.getRequestedBy()))
                .filter(approval -> status == null || approval.getStatus() == status)
                .limit(normalizeLimit(limit, DEFAULT_USER_LIMIT))
                .map(SchemaApprovalResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public SchemaApprovalResponse getApprovalForUser(Long approvalId, String currentUser) {
        SchemaApprovalEntity entity = getApprovalEntity(approvalId);
        if (!currentUserService.isAdmin()) {
            String resolvedUser = requireUsername(currentUser);
            if (!resolvedUser.equalsIgnoreCase(entity.getRequestedBy())) {
                throw new AccessDeniedException("Access denied");
            }
        }
        return SchemaApprovalResponse.fromEntity(entity);
    }

    @Transactional(readOnly = true)
    public List<SchemaApprovalResponse> listAdminApprovals(
            SchemaApprovalStatus status,
            String subject,
            String requestedBy,
            Integer limit
    ) {
        return schemaApprovalRepository.findAll(Sort.by(Sort.Direction.DESC, "requestedAt")).stream()
                .filter(approval -> status == null || approval.getStatus() == status)
                .filter(approval -> !hasText(subject) || approval.getSubject().equalsIgnoreCase(subject.trim()))
                .filter(approval -> !hasText(requestedBy) || matchesRequestedBy(approval, requestedBy))
                .limit(normalizeLimit(limit, DEFAULT_ADMIN_LIMIT))
                .map(SchemaApprovalResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public SchemaApprovalResponse getAdminApproval(Long approvalId) {
        return SchemaApprovalResponse.fromEntity(getApprovalEntity(approvalId));
    }

    @Transactional
    public SchemaApprovalResponse approve(Long approvalId, ApprovalDecisionRequest request) {
        SchemaApprovalEntity entity = getApprovalEntity(approvalId);
        validatePendingApproval(entity);

        SchemaVersionInfo latestVersion = getCurrentLatestVersion(entity);
        if (entity.getOldVersion() == null || latestVersion.version() != entity.getOldVersion()) {
            throw new ApprovalBaselineChangedException();
        }

        Instant now = Instant.now();
        String reviewer = currentUserService.currentUsername();
        try {
            SchemaVersionInfo registeredVersion = registerSchemaVersionService.register(
                    new RegisterSchemaVersionCommand(
                            entity.getSubject(),
                            SchemaType.valueOf(entity.getSchemaType()),
                            resolveCompatibilityMode(entity.getCompatibilityMode()),
                            "Approved via schema promotion workflow",
                            entity.getNewSchemaText(),
                            SchemaVersionStatus.ACTIVE,
                            SchemaSourceType.CONFLUENT,
                            null
                    )
            );
            entity.setStatus(SchemaApprovalStatus.PUBLISHED);
            entity.setReviewedBy(reviewer);
            entity.setReviewedAt(now);
            entity.setAdminComment(normalizeComment(request));
            entity.setRegisteredVersion(registeredVersion.version());
            entity.setSchemaRegistryId(parseRegistryId(registeredVersion.externalSchemaId()));
            entity.setUpdatedAt(now);
            SchemaApprovalEntity saved = schemaApprovalRepository.save(entity);
            updatePromotionMetadata(saved.getAnalysisId(), true, SchemaPromotionStatus.REGISTERED,
                    saved.getRegisteredVersion(), saved.getSchemaRegistryId());
            return SchemaApprovalResponse.fromEntity(saved);
        } catch (SchemaRegistryConflictException exception) {
            markRegistryRejected(entity, reviewer, request, now);
            throw exception;
        } catch (RestClientResponseException exception) {
            markRegistryRejected(entity, reviewer, request, now);
            throw new SchemaRegistryConflictException(
                    resolveRegistryMessage(exception),
                    exception.getStatusCode().value(),
                    exception.getResponseBodyAsString()
            );
        } catch (SchemaRegistryUnavailableException exception) {
            throw exception;
        }
    }

    @Transactional
    public SchemaApprovalResponse reject(Long approvalId, ApprovalDecisionRequest request) {
        SchemaApprovalEntity entity = getApprovalEntity(approvalId);
        validatePendingApproval(entity);

        Instant now = Instant.now();
        entity.setStatus(SchemaApprovalStatus.REJECTED);
        entity.setReviewedBy(currentUserService.currentUsername());
        entity.setReviewedAt(now);
        entity.setAdminComment(normalizeComment(request));
        entity.setUpdatedAt(now);
        return SchemaApprovalResponse.fromEntity(schemaApprovalRepository.save(entity));
    }

    private SchemaApprovalEntity getApprovalEntity(Long approvalId) {
        return schemaApprovalRepository.findById(approvalId)
                .orElseThrow(() -> new SchemaApprovalNotFoundException(approvalId));
    }

    private void validatePendingApproval(SchemaApprovalEntity entity) {
        if (entity.getStatus() != SchemaApprovalStatus.PENDING) {
            throw new InvalidApprovalStateException("Approval is already processed or unavailable for this action");
        }
        if (!entity.isFormalCompatible()) {
            throw new ApprovalNotAllowedException("Administrator cannot approve a formally incompatible schema");
        }
        if (!GovernanceDecision.ALLOW_WITH_CAUTION.name().equals(entity.getGovernanceDecision())) {
            throw new ApprovalNotAllowedException(
                    "Administrator can only approve changes with governanceDecision = ALLOW_WITH_CAUTION"
            );
        }
    }

    private SchemaVersionInfo getCurrentLatestVersion(SchemaApprovalEntity entity) {
        try {
            return schemaCatalog.getLatestVersion(entity.getSubject());
        } catch (ResourceNotFoundException exception) {
            throw new ApprovalBaselineChangedException();
        }
    }

    private CompatibilityMode resolveCompatibilityMode(String compatibilityMode) {
        if (!hasText(compatibilityMode)) {
            return CompatibilityMode.BACKWARD;
        }
        return CompatibilityMode.valueOf(compatibilityMode.toUpperCase(Locale.ROOT));
    }

    private void markRegistryRejected(
            SchemaApprovalEntity entity,
            String reviewer,
            ApprovalDecisionRequest request,
            Instant now
    ) {
        entity.setStatus(SchemaApprovalStatus.REGISTRY_REJECTED);
        entity.setReviewedBy(reviewer);
        entity.setReviewedAt(now);
        entity.setAdminComment(normalizeComment(request));
        entity.setUpdatedAt(now);
        schemaApprovalRepository.save(entity);
    }

    private void updatePromotionMetadata(
            Long analysisId,
            Boolean registered,
            SchemaPromotionStatus status,
            Integer registeredVersion,
            Integer schemaRegistryId
    ) {
        if (analysisId == null) {
            return;
        }
        analysisRepository.updatePromotionMetadata(
                new UpdatePromotionMetadataCommand(
                        analysisId,
                        true,
                        registered,
                        status,
                        registeredVersion,
                        schemaRegistryId
                )
        );
    }

    private String requireUsername(String username) {
        if (!hasText(username)) {
            throw new AuthenticationRequiredException();
        }
        return username.trim();
    }

    private boolean matchesRequestedBy(SchemaApprovalEntity approval, String requestedBy) {
        return approval.getRequestedBy() != null
                && approval.getRequestedBy().equalsIgnoreCase(requestedBy.trim());
    }

    private int normalizeLimit(Integer limit, int defaultValue) {
        if (limit == null) {
            return defaultValue;
        }
        return Math.min(Math.max(limit, 1), MAX_LIMIT);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String normalizeUsername(String username) {
        if (!hasText(username)) {
            return "system";
        }
        return username.trim();
    }

    private String normalizeComment(ApprovalDecisionRequest request) {
        if (request == null || !hasText(request.getComment())) {
            return null;
        }
        return request.getComment().trim();
    }

    private Integer parseRegistryId(String externalSchemaId) {
        if (!hasText(externalSchemaId)) {
            return null;
        }
        try {
            return Integer.valueOf(externalSchemaId);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String resolveRegistryMessage(RestClientResponseException exception) {
        String responseBody = exception.getResponseBodyAsString();
        if (hasText(responseBody)) {
            return responseBody;
        }
        return exception.getStatusText();
    }
}
