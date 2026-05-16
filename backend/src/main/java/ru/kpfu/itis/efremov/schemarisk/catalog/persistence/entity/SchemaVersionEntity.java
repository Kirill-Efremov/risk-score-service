package ru.kpfu.itis.efremov.schemarisk.catalog.persistence.entity;

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
import ru.kpfu.itis.efremov.schemarisk.catalog.model.SchemaSourceType;
import ru.kpfu.itis.efremov.schemarisk.catalog.model.SchemaVersionStatus;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "schema_version")
public class SchemaVersionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private SchemaSubjectEntity subject;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "schema_text", nullable = false, columnDefinition = "text")
    private String schemaText;

    @Column(name = "schema_hash", nullable = false, length = 64)
    private String schemaHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SchemaVersionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private SchemaSourceType sourceType;

    @Column(name = "external_schema_id")
    private String externalSchemaId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}




