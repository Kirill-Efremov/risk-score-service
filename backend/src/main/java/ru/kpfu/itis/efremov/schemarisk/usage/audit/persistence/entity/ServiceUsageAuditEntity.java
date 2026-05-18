package ru.kpfu.itis.efremov.schemarisk.usage.audit.persistence.entity;

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
import ru.kpfu.itis.efremov.schemarisk.usage.audit.model.ServiceUsageAuditAction;
import ru.kpfu.itis.efremov.schemarisk.usage.model.ServiceRole;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "service_usage_audit")
public class ServiceUsageAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_id")
    private Long serviceId;

    @Column(name = "service_name")
    private String serviceName;

    @Column(name = "usage_id")
    private Long usageId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private ServiceUsageAuditAction action;

    @Column(name = "old_subject")
    private String oldSubject;

    @Column(name = "new_subject")
    private String newSubject;

    @Column(name = "old_version")
    private Integer oldVersion;

    @Column(name = "new_version")
    private Integer newVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_role")
    private ServiceRole oldRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_role")
    private ServiceRole newRole;

    @Column(name = "old_active")
    private Boolean oldActive;

    @Column(name = "new_active")
    private Boolean newActive;

    @Column(name = "old_service_active")
    private Boolean oldServiceActive;

    @Column(name = "new_service_active")
    private Boolean newServiceActive;

    @Column(name = "changed_by")
    private String changedBy;

    @Column(name = "reason")
    private String reason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
