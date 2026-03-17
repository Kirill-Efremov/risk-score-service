package ru.kpfu.itis.efremov.schemarisk.infrastructure.analysis.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import ru.kpfu.itis.efremov.schemarisk.core.risk.RiskLevel;
import ru.kpfu.itis.efremov.schemarisk.infrastructure.catalog.persistence.entity.SchemaSubjectEntity;
import ru.kpfu.itis.efremov.schemarisk.infrastructure.catalog.persistence.entity.SchemaVersionEntity;
import ru.kpfu.itis.efremov.schemarisk.model.CompatibilityMode;
import ru.kpfu.itis.efremov.schemarisk.model.Decision;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "schema_analysis")
public class SchemaAnalysisEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private SchemaSubjectEntity subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "old_version_id")
    private SchemaVersionEntity oldVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_version_id")
    private SchemaVersionEntity newVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "compatibility_mode", nullable = false)
    private CompatibilityMode compatibilityMode;

    @Column(name = "formal_compatible", nullable = false)
    private boolean formalCompatible;

    @Column(name = "issues_json", nullable = false, columnDefinition = "text")
    private String issuesJson;

    @Column(name = "diff_json", columnDefinition = "text")
    private String diffJson;

    @Column(name = "risk_score", nullable = false)
    private int riskScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false)
    private RiskLevel riskLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false)
    private Decision decision;

    @Column(name = "recommendations_json", nullable = false, columnDefinition = "text")
    private String recommendationsJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by")
    private String createdBy;
}
