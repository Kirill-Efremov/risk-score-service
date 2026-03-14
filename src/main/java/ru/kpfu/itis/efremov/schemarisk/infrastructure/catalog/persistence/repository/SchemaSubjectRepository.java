package ru.kpfu.itis.efremov.schemarisk.infrastructure.catalog.persistence.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.kpfu.itis.efremov.schemarisk.infrastructure.catalog.persistence.entity.SchemaSubjectEntity;

import java.util.Optional;

public interface SchemaSubjectRepository extends JpaRepository<SchemaSubjectEntity, Long> {

    Optional<SchemaSubjectEntity> findByName(String name);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SchemaSubjectEntity s where s.name = :name")
    Optional<SchemaSubjectEntity> findByNameForUpdate(@Param("name") String name);
}
