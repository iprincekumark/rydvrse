package com.rydvrse.support.infrastructure;

import com.rydvrse.support.domain.IncidentCaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IncidentCaseRepository extends JpaRepository<IncidentCaseEntity, UUID> {

    Optional<IncidentCaseEntity> findBySupportTicketId(UUID supportTicketId);

    List<IncidentCaseEntity> findByStatusInOrderByOpenedAtDesc(List<String> statuses);
}
