package com.rydvrse.support.infrastructure;

import com.rydvrse.support.domain.SupportTicketNoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SupportTicketNoteRepository extends JpaRepository<SupportTicketNoteEntity, UUID> {

    List<SupportTicketNoteEntity> findBySupportTicketIdOrderByCreatedAtAsc(UUID supportTicketId);
}
