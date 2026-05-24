package ru.kpfu.itis.efremov.schemarisk.approval.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.kpfu.itis.efremov.schemarisk.approval.model.SchemaApprovalStatus;

import java.util.List;

public interface SchemaApprovalRepository extends JpaRepository<SchemaApprovalEntity, Long> {

    List<SchemaApprovalEntity> findByRequestedByOrderByRequestedAtDesc(String requestedBy, Pageable pageable);

    List<SchemaApprovalEntity> findByStatusOrderByRequestedAtDesc(SchemaApprovalStatus status, Pageable pageable);

    List<SchemaApprovalEntity> findBySubjectOrderByRequestedAtDesc(String subject, Pageable pageable);
}
