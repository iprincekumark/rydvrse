package com.rydvrse.notification.jobs;

import com.rydvrse.common.config.RydvrseProperties;
import com.rydvrse.notification.application.NotificationDeliveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NotificationDispatchJob {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatchJob.class);

    private final NotificationDeliveryService notificationDeliveryService;
    private final RydvrseProperties rydvrseProperties;

    public NotificationDispatchJob(NotificationDeliveryService notificationDeliveryService, RydvrseProperties rydvrseProperties) {
        this.notificationDeliveryService = notificationDeliveryService;
        this.rydvrseProperties = rydvrseProperties;
    }

    @Scheduled(fixedDelayString = "${rydvrse.jobs.notification-dispatch-delay-ms:30000}")
    public void run() {
        if (!rydvrseProperties.getJobs().isEnabled()) {
            return;
        }
        int processed = notificationDeliveryService.dispatchQueuedDeliveries(50);
        if (processed > 0) {
            log.info("Processed {} queued notification deliveries", processed);
        }
    }
}
