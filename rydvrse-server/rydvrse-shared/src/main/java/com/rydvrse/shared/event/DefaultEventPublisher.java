package com.rydvrse.shared.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Default event publisher using Spring ApplicationEventPublisher.
 * For MVP — all events are in-process. Kafka migration replaces this single class.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultEventPublisher implements EventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(DomainEvent event) {
        log.debug("Publishing event: {} [{}]", event.getEventType(), event.getEventId());
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    public void publishAsync(DomainEvent event) {
        log.debug("Publishing async event: {} [{}]", event.getEventType(), event.getEventId());
        applicationEventPublisher.publishEvent(event);
    }
}
