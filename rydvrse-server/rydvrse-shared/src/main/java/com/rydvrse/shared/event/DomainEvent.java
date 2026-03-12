package com.rydvrse.shared.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Base interface for all domain events in RYDVRSE.
 * Every module publishes domain events implementing this interface.
 * Events are transported via Spring ApplicationEvents (in-process)
 * and optionally via Kafka (for async/cross-service communication).
 */
public interface DomainEvent {

    /** Unique event identifier */
    UUID getEventId();

    /** Type of the event, e.g., "TRIP_REQUESTED" */
    String getEventType();

    /** ID of the aggregate root that produced this event */
    UUID getAggregateId();

    /** Timestamp when the event occurred */
    Instant getOccurredAt();

    /** Source module that produced the event */
    String getSourceModule();
}
