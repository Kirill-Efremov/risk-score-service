package ru.kpfu.itis.efremov.schemarisk.history.persistence.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.kpfu.itis.efremov.schemarisk.history.persistence.entity.SchemaAnalysisEntity;

import java.util.List;
import java.util.Optional;

public interface SchemaAnalysisRepository extends JpaRepository<SchemaAnalysisEntity, Long> {

    @EntityGraph(attributePaths = {"subject", "oldVersion", "newVersion"})
    Optional<SchemaAnalysisEntity> findById(Long id);

    @EntityGraph(attributePaths = {"subject", "oldVersion", "newVersion"})
    @Query("""
            select analysis
            from SchemaAnalysisEntity analysis
            left join analysis.subject subject
            where analysis.subjectName = :subjectName
               or subject.name = :subjectName
            order by analysis.createdAt desc
            """)
    List<SchemaAnalysisEntity> findAllBySubjectReferenceOrderByCreatedAtDesc(@Param("subjectName") String subjectName);
}
