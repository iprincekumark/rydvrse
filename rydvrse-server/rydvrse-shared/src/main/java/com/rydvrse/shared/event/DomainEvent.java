package com.rydvrse.shared.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Contract for all domain events across the platform.
 * Designed for seamless Kafka migration.
 */
public interface DomainEvent {
    UUID getEventId();
    String getEventType();
    Instant getOccurredAt();
    UUID getAggregateId();
    String getAggregateType();
    String getSourceModule();
}
