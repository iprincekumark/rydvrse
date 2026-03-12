package com.rydvrse.shared.event;

/**
 * Abstraction for publishing domain events.
 * In the modular monolith, this uses Spring's ApplicationEventPublisher.
 * When extracting to microservices, swap to Kafka-backed implementation.
 */
public interface EventPublisher {

    /**
     * Publish a domain event to in-process listeners.
     */
    void publish(DomainEvent event);

    /**
     * Publish a domain event to Kafka for async consumers (analytics, notifications).
     */
    void publishAsync(DomainEvent event);
}
