package com.rydvrse.notification.jobs;

import com.rydvrse.common.config.RydvrseProperties;
import com.rydvrse.notification.application.NotificationDeliveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NotificationRetryJob {

    private static final Logger log = LoggerFactory.getLogger(NotificationRetryJob.class);

    private final NotificationDeliveryService notificationDeliveryService;
    private final RydvrseProperties rydvrseProperties;

    public NotificationRetryJob(NotificationDeliveryService notificationDeliveryService, RydvrseProperties rydvrseProperties) {
        this.notificationDeliveryService = notificationDeliveryService;
        this.rydvrseProperties = rydvrseProperties;
    }

    @Scheduled(fixedDelayString = "${rydvrse.jobs.notification-retry-delay-ms:60000}")
    public void run() {
        if (!rydvrseProperties.getJobs().isEnabled()) {
            return;
        }
        int retried = notificationDeliveryService.retryFailedDeliveries(50);
        if (retried > 0) {
            log.info("Retried {} failed notification deliveries", retried);
        }
    }
}
