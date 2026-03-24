package ru.kpfu.itis.efremov.schemarisk.history.persistence.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.kpfu.itis.efremov.schemarisk.history.persistence.entity.SchemaAnalysisEntity;

import java.util.List;
import java.util.Optional;

public interface SchemaAnalysisRepository extends JpaRepository<SchemaAnalysisEntity, Long> {

    @EntityGraph(attributePaths = {"subject", "oldVersion", "newVersion"})
    Optional<SchemaAnalysisEntity> findById(Long id);

    @EntityGraph(attributePaths = {"subject", "oldVersion", "newVersion"})
    List<SchemaAnalysisEntity> findAllBySubject_NameOrderByCreatedAtDesc(String subjectName);
}




