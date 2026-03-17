package ru.kpfu.itis.efremov.schemarisk.infrastructure.analysis.persistence.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.kpfu.itis.efremov.schemarisk.infrastructure.analysis.persistence.entity.SchemaAnalysisEntity;

import java.util.List;
import java.util.Optional;

public interface SchemaAnalysisRepository extends JpaRepository<SchemaAnalysisEntity, Long> {

    @EntityGraph(attributePaths = {"subject", "oldVersion", "newVersion"})
    Optional<SchemaAnalysisEntity> findById(Long id);

    @EntityGraph(attributePaths = {"subject", "oldVersion", "newVersion"})
    List<SchemaAnalysisEntity> findAllBySubject_NameOrderByCreatedAtDesc(String subjectName);
}
