package com.rydvrse.shared.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Default EventPublisher implementation.
 * - publish() → Spring ApplicationEvent (synchronous, in-process)
 * - publishAsync() → Kafka topic (asynchronous, durable)
 *
 * This dual-channel approach lets modules communicate synchronously for
 * critical paths (e.g., trip state transitions) while also streaming
 * events to Kafka for analytics, notifications, and audit.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultEventPublisher implements EventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String EVENT_TOPIC_PREFIX = "rydvrse.events.";

    @Override
    public void publish(DomainEvent event) {
        log.info("Publishing domain event [{}] from module [{}] for aggregate [{}]",
                event.getEventType(), event.getSourceModule(), event.getAggregateId());
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    public void publishAsync(DomainEvent event) {
        try {
            String topic = EVENT_TOPIC_PREFIX + event.getSourceModule().toLowerCase();
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, event.getAggregateId().toString(), payload);
            log.info("Published async event [{}] to Kafka topic [{}]",
                    event.getEventType(), topic);
        } catch (Exception e) {
            log.error("Failed to publish async event [{}]: {}",
                    event.getEventType(), e.getMessage(), e);
        }
    }
}
