package ru.kpfu.itis.efremov.schemarisk.usage.audit.persistence.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.kpfu.itis.efremov.schemarisk.usage.audit.model.ServiceUsageAuditAction;
import ru.kpfu.itis.efremov.schemarisk.usage.audit.persistence.entity.ServiceUsageAuditEntity;

import java.util.List;

public interface ServiceUsageAuditRepository extends JpaRepository<ServiceUsageAuditEntity, Long> {

    @Query("""
            select a from ServiceUsageAuditEntity a
            where a.serviceId = :serviceId
            order by a.createdAt desc, a.id desc
            """)
    List<ServiceUsageAuditEntity> findServiceAudit(
            @Param("serviceId") Long serviceId,
            Pageable pageable
    );

    @Query("""
            select a from ServiceUsageAuditEntity a
            where a.serviceId = :serviceId
              and a.action = :action
            order by a.createdAt desc, a.id desc
            """)
    List<ServiceUsageAuditEntity> findServiceAuditByAction(
            @Param("serviceId") Long serviceId,
            @Param("action") ServiceUsageAuditAction action,
            Pageable pageable
    );

    @Query("""
            select a from ServiceUsageAuditEntity a
            where a.serviceId = :serviceId
              and a.usageId = :usageId
            order by a.createdAt desc, a.id desc
            """)
    List<ServiceUsageAuditEntity> findUsageAudit(
            @Param("serviceId") Long serviceId,
            @Param("usageId") Long usageId,
            Pageable pageable
    );

    @Query("""
            select a from ServiceUsageAuditEntity a
            order by a.createdAt desc, a.id desc
            """)
    List<ServiceUsageAuditEntity> findAllAudit(Pageable pageable);

    @Query("""
            select a from ServiceUsageAuditEntity a
            where a.serviceId = :serviceId
            order by a.createdAt desc, a.id desc
            """)
    List<ServiceUsageAuditEntity> findAllAuditByServiceId(
            @Param("serviceId") Long serviceId,
            Pageable pageable
    );

    @Query("""
            select a from ServiceUsageAuditEntity a
            where a.usageId = :usageId
            order by a.createdAt desc, a.id desc
            """)
    List<ServiceUsageAuditEntity> findAllAuditByUsageId(
            @Param("usageId") Long usageId,
            Pageable pageable
    );

    @Query("""
            select a from ServiceUsageAuditEntity a
            where a.action = :action
            order by a.createdAt desc, a.id desc
            """)
    List<ServiceUsageAuditEntity> findAllAuditByAction(
            @Param("action") ServiceUsageAuditAction action,
            Pageable pageable
    );

    @Query("""
            select a from ServiceUsageAuditEntity a
            where a.serviceId = :serviceId
              and a.usageId = :usageId
            order by a.createdAt desc, a.id desc
            """)
    List<ServiceUsageAuditEntity> findAllAuditByServiceIdAndUsageId(
            @Param("serviceId") Long serviceId,
            @Param("usageId") Long usageId,
            Pageable pageable
    );

    @Query("""
            select a from ServiceUsageAuditEntity a
            where a.serviceId = :serviceId
              and a.action = :action
            order by a.createdAt desc, a.id desc
            """)
    List<ServiceUsageAuditEntity> findAllAuditByServiceIdAndAction(
            @Param("serviceId") Long serviceId,
            @Param("action") ServiceUsageAuditAction action,
            Pageable pageable
    );

    @Query("""
            select a from ServiceUsageAuditEntity a
            where a.usageId = :usageId
              and a.action = :action
            order by a.createdAt desc, a.id desc
            """)
    List<ServiceUsageAuditEntity> findAllAuditByUsageIdAndAction(
            @Param("usageId") Long usageId,
            @Param("action") ServiceUsageAuditAction action,
            Pageable pageable
    );

    @Query("""
            select a from ServiceUsageAuditEntity a
            where a.serviceId = :serviceId
              and a.usageId = :usageId
              and a.action = :action
            order by a.createdAt desc, a.id desc
            """)
    List<ServiceUsageAuditEntity> findAllAuditByServiceIdAndUsageIdAndAction(
            @Param("serviceId") Long serviceId,
            @Param("usageId") Long usageId,
            @Param("action") ServiceUsageAuditAction action,
            Pageable pageable
    );
}
