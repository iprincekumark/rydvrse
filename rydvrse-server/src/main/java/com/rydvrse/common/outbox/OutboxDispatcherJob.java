package com.rydvrse.common.outbox;

import com.rydvrse.common.config.RydvrseProperties;
import com.rydvrse.dispatch.application.DispatchOrchestrator;
import com.rydvrse.notification.application.NotificationPlannerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class OutboxDispatcherJob {

    private static final Logger log = LoggerFactory.getLogger(OutboxDispatcherJob.class);

    private final OutboxEventRepository outboxEventRepository;
    private final DispatchOrchestrator dispatchOrchestrator;
    private final NotificationPlannerService notificationPlannerService;
    private final RydvrseProperties rydvrseProperties;

    public OutboxDispatcherJob(
            OutboxEventRepository outboxEventRepository,
            DispatchOrchestrator dispatchOrchestrator,
            NotificationPlannerService notificationPlannerService,
            RydvrseProperties rydvrseProperties
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.dispatchOrchestrator = dispatchOrchestrator;
        this.notificationPlannerService = notificationPlannerService;
        this.rydvrseProperties = rydvrseProperties;
    }

    @Scheduled(fixedDelayString = "${rydvrse.jobs.outbox-dispatch-delay-ms:15000}")
    @Transactional
    public void run() {
        if (!rydvrseProperties.getJobs().isEnabled()) {
            return;
        }
        List<OutboxEventEntity> events = outboxEventRepository.findTop50ByOutboxStatusAndAvailableAtBeforeOrderByCreatedAtAsc("PENDING", OffsetDateTime.now());
        for (OutboxEventEntity event : events) {
            try {
                handleEvent(event);
                event.setOutboxStatus("PROCESSED");
                event.setProcessedAt(OffsetDateTime.now());
            } catch (RuntimeException ex) {
                event.setRetryCount(event.getRetryCount() + 1);
                event.setAvailableAt(OffsetDateTime.now().plusMinutes(1));
                if (event.getRetryCount() >= 5) {
                    event.setOutboxStatus("DEAD");
                }
                log.error("Failed to process outbox event {}", event.getId(), ex);
            }
            outboxEventRepository.save(event);
        }
    }

    private void handleEvent(OutboxEventEntity event) {
        if ("BookingConfirmedEvent".equals(event.getEventType())) {
            UUID bookingId = UUID.fromString(event.getPayload().get("booking_id").asText());
            dispatchOrchestrator.orchestrate(bookingId, event.getEventType());
        }
        notificationPlannerService.planFromOutbox(event);
    }
}
