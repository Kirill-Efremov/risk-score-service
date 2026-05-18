package ru.kpfu.itis.efremov.schemarisk.usage.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.kpfu.itis.efremov.schemarisk.usage.persistence.entity.ServiceEntity;

import java.util.List;
import java.util.Optional;

public interface ServiceEntityRepository extends JpaRepository<ServiceEntity, Long> {

    Optional<ServiceEntity> findByName(String name);

    @Query("""
            select s from ServiceEntity s
            where (:active is null or s.active = :active)
              and (:critical is null or s.critical = :critical)
            order by s.name asc
            """)
    List<ServiceEntity> findAllByFilters(@Param("active") Boolean active, @Param("critical") Boolean critical);
}
