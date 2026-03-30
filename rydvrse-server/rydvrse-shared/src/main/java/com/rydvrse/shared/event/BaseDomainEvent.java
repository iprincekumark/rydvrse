package com.rydvrse.shared.event;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Abstract base for all domain events. Designed for future Kafka migration —
 * all fields map directly to Kafka message headers/payload.
 */
@Getter
@Setter
public abstract class BaseDomainEvent implements DomainEvent {

    private UUID eventId;
    private String eventType;
    private Instant occurredAt;
    private UUID aggregateId;
    private String aggregateType;
    private String sourceModule;

    protected BaseDomainEvent() {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
    }

    protected BaseDomainEvent(String eventType, UUID aggregateId, String aggregateType, String sourceModule) {
        this();
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.sourceModule = sourceModule;
    }
}
