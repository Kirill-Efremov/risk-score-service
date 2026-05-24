package ru.kpfu.itis.efremov.schemarisk.approval.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import ru.kpfu.itis.efremov.schemarisk.approval.model.SchemaApprovalStatus;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "schema_promotion_approval")
public class SchemaApprovalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "schema_type", nullable = false, length = 32)
    private String schemaType;

    @Column(name = "compatibility_mode", length = 32)
    private String compatibilityMode;

    @Column(name = "old_version")
    private Integer oldVersion;

    @Column(name = "new_schema_text", nullable = false, columnDefinition = "text")
    private String newSchemaText;

    @Column(name = "analysis_id")
    private Long analysisId;

    @Column(name = "formal_compatible", nullable = false)
    private boolean formalCompatible;

    @Column(name = "governance_decision", nullable = false, length = 64)
    private String governanceDecision;

    @Column(name = "risk_score", nullable = false)
    private int riskScore;

    @Column(name = "risk_level", nullable = false, length = 32)
    private String riskLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private SchemaApprovalStatus status;

    @Column(name = "requested_by")
    private String requestedBy;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    @Column(name = "reviewed_by")
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "admin_comment", columnDefinition = "text")
    private String adminComment;

    @Column(name = "registered_version")
    private Integer registeredVersion;

    @Column(name = "schema_registry_id")
    private Integer schemaRegistryId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
