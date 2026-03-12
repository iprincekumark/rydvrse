package com.rydvrse.shared.event;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Abstract base implementation of DomainEvent.
 * All concrete domain events should extend this class.
 */
@Getter
@Setter
public abstract class BaseDomainEvent implements DomainEvent {

    private UUID eventId;
    private String eventType;
    private UUID aggregateId;
    private Instant occurredAt;
    private String sourceModule;

    protected BaseDomainEvent() {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
    }

    protected BaseDomainEvent(String eventType, UUID aggregateId, String sourceModule) {
        this();
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.sourceModule = sourceModule;
    }
}
