package com.rydvrse.common.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, UUID> {

    List<OutboxEventEntity> findTop50ByOutboxStatusAndAvailableAtBeforeOrderByCreatedAtAsc(String outboxStatus, OffsetDateTime availableAt);
}
