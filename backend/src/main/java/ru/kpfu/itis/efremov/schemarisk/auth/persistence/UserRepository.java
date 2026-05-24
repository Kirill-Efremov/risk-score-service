package ru.kpfu.itis.efremov.schemarisk.auth.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.kpfu.itis.efremov.schemarisk.auth.model.UserRole;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(String username);

    boolean existsByUsername(String username);

    long countByRoleAndActive(UserRole role, boolean active);

    @Modifying
    @Query(value = "lock table app_user in exclusive mode", nativeQuery = true)
    void lockTable();
}
