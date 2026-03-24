package ru.kpfu.itis.efremov.schemarisk.usage.persistence.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.kpfu.itis.efremov.schemarisk.usage.model.ServiceRole;
import ru.kpfu.itis.efremov.schemarisk.usage.model.UsageStatus;
import ru.kpfu.itis.efremov.schemarisk.usage.persistence.entity.ServiceSchemaUsageEntity;

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
            where u.subject = :subject
              and u.role = :role
              and u.active = true
              and u.status = :status
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
              and (:version is null or u.version is null or u.version = :version)
            order by u.createdAt desc
            """)
    List<ServiceSchemaUsageEntity> findActiveBySubjectAndRoleAndVersion(
            @Param("subject") String subject,
            @Param("role") ServiceRole role,
            @Param("status") UsageStatus status,
            @Param("version") Integer version
    );
}




