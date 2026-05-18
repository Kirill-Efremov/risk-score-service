package ru.kpfu.itis.efremov.schemarisk.usage.persistence.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.kpfu.itis.efremov.schemarisk.usage.model.ServiceRole;
import ru.kpfu.itis.efremov.schemarisk.usage.model.UsageStatus;
import ru.kpfu.itis.efremov.schemarisk.usage.persistence.entity.ServiceSchemaUsageEntity;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ServiceSchemaUsageJpaRepository extends JpaRepository<ServiceSchemaUsageEntity, Long> {

    @EntityGraph(attributePaths = "service")
    List<ServiceSchemaUsageEntity> findAllBySubjectOrderByCreatedAtDesc(String subject);

    @Override
    @EntityGraph(attributePaths = "service")
    Optional<ServiceSchemaUsageEntity> findById(Long id);

    @EntityGraph(attributePaths = "service")
    @Query("""
            select u from ServiceSchemaUsageEntity u
            where u.service.id = :serviceId
              and (:active is null or u.active = :active)
              and (:role is null or u.role = :role)
              and (
                   coalesce(:subject, '') = ''
                   or lower(u.subject) like concat('%', lower(coalesce(:subject, '')), '%')
              )
            order by u.createdAt desc
            """)
    List<ServiceSchemaUsageEntity> findServiceUsages(
            @Param("serviceId") Long serviceId,
            @Param("active") Boolean active,
            @Param("role") ServiceRole role,
            @Param("subject") String subject
    );

    @EntityGraph(attributePaths = "service")
    @Query("""
            select u from ServiceSchemaUsageEntity u
            where u.subject = :subject
              and u.role = :role
              and u.active = true
              and u.status = :status
              and u.service.active = true
            order by u.createdAt desc
            """)
    List<ServiceSchemaUsageEntity> findActiveBySubjectAndRole(
            @Param("subject") String subject,
            @Param("role") ServiceRole role,
            @Param("status") UsageStatus status
    );

    @EntityGraph(attributePaths = "service")
    @Query("""
            select u from ServiceSchemaUsageEntity u
            where u.subject = :subject
              and u.role = :role
              and u.active = true
              and u.status = :status
              and u.service.active = true
              and (:version is null or u.version is null or u.version = :version)
            order by u.createdAt desc
            """)
    List<ServiceSchemaUsageEntity> findActiveBySubjectAndRoleAndVersion(
            @Param("subject") String subject,
            @Param("role") ServiceRole role,
            @Param("status") UsageStatus status,
            @Param("version") Integer version
    );

    @Modifying
    @Query("""
            update ServiceSchemaUsageEntity u
            set u.active = false,
                u.status = :status,
                u.activeTo = :now,
                u.updatedAt = :now
            where u.service.id = :serviceId
              and u.active = true
            """)
    int deactivateActiveUsagesByServiceId(
            @Param("serviceId") Long serviceId,
            @Param("status") UsageStatus status,
            @Param("now") Instant now
    );

    @Query("""
            select count(u) > 0 from ServiceSchemaUsageEntity u
            where u.service.id = :serviceId
              and u.active = true
              and u.subject = :subject
              and u.role = :role
              and (
                   (:version is null and u.version is null)
                   or u.version = :version
              )
              and (:excludeUsageId is null or u.id <> :excludeUsageId)
            """)
    boolean existsActiveDuplicate(
            @Param("serviceId") Long serviceId,
            @Param("subject") String subject,
            @Param("version") Integer version,
            @Param("role") ServiceRole role,
            @Param("excludeUsageId") Long excludeUsageId
    );
}
