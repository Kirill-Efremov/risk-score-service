package ru.kpfu.itis.efremov.schemarisk.approval.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.kpfu.itis.efremov.schemarisk.approval.model.SchemaApprovalStatus;
import ru.kpfu.itis.efremov.schemarisk.approval.persistence.SchemaApprovalEntity;

import java.time.Instant;

@Schema(description = "Заявка на административное согласование публикации схемы")
public record SchemaApprovalResponse(
        Long id,
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
        SchemaApprovalStatus status,
        String requestedBy,
        Instant requestedAt,
        String reviewedBy,
        Instant reviewedAt,
        String adminComment,
        Integer registeredVersion,
        Integer schemaRegistryId
) {
    public static SchemaApprovalResponse fromEntity(SchemaApprovalEntity entity) {
        return new SchemaApprovalResponse(
                entity.getId(),
                entity.getSubject(),
                entity.getSchemaType(),
                entity.getCompatibilityMode(),
                entity.getOldVersion(),
                entity.getNewSchemaText(),
                entity.getAnalysisId(),
                entity.isFormalCompatible(),
                entity.getGovernanceDecision(),
                entity.getRiskScore(),
                entity.getRiskLevel(),
                entity.getStatus(),
                entity.getRequestedBy(),
                entity.getRequestedAt(),
                entity.getReviewedBy(),
                entity.getReviewedAt(),
                entity.getAdminComment(),
                entity.getRegisteredVersion(),
                entity.getSchemaRegistryId()
        );
    }
}
