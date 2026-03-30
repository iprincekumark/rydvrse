package com.rydvrse.shared.event;

/**
 * Abstraction for event publishing. Uses Spring ApplicationEventPublisher for MVP;
 * designed for drop-in Kafka replacement.
 */
public interface EventPublisher {

    /**
     * Publish event synchronously (within the current transaction).
     */
    void publish(DomainEvent event);

    /**
     * Publish event asynchronously after the current transaction commits.
     */
    void publishAsync(DomainEvent event);
}
