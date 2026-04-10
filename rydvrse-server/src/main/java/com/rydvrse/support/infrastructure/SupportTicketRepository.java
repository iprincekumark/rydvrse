package com.rydvrse.support.infrastructure;

import com.rydvrse.support.domain.SupportTicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.UUID;

public interface SupportTicketRepository extends JpaRepository<SupportTicketEntity, UUID> {

    long countByStatusIn(Collection<String> statuses);
}
