package ru.kpfu.itis.efremov.schemarisk.infrastructure.catalog.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.kpfu.itis.efremov.schemarisk.infrastructure.catalog.persistence.entity.SchemaVersionEntity;

import java.util.List;
import java.util.Optional;

public interface SchemaVersionRepository extends JpaRepository<SchemaVersionEntity, Long> {

    Optional<SchemaVersionEntity> findTopBySubject_NameOrderByVersionDesc(String subjectName);

    Optional<SchemaVersionEntity> findBySubject_NameAndVersion(String subjectName, Integer version);

    List<SchemaVersionEntity> findAllBySubject_NameOrderByVersionDesc(String subjectName);

    @Query("select max(v.version) from SchemaVersionEntity v where v.subject.id = :subjectId")
    Integer findMaxVersionBySubjectId(@Param("subjectId") Long subjectId);
}
