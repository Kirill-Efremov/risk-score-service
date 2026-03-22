package ru.kpfu.itis.efremov.schemarisk.infrastructure.usage.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kpfu.itis.efremov.schemarisk.infrastructure.usage.persistence.entity.ServiceEntity;

import java.util.Optional;

public interface ServiceEntityRepository extends JpaRepository<ServiceEntity, Long> {

    Optional<ServiceEntity> findByName(String name);
}
