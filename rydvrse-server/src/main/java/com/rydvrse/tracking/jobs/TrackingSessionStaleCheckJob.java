package com.rydvrse.tracking.jobs;

import com.rydvrse.common.config.RydvrseProperties;
import com.rydvrse.tracking.application.TrackingSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TrackingSessionStaleCheckJob {

    private static final Logger log = LoggerFactory.getLogger(TrackingSessionStaleCheckJob.class);

    private final TrackingSessionService trackingSessionService;
    private final RydvrseProperties rydvrseProperties;

    public TrackingSessionStaleCheckJob(TrackingSessionService trackingSessionService, RydvrseProperties rydvrseProperties) {
        this.trackingSessionService = trackingSessionService;
        this.rydvrseProperties = rydvrseProperties;
    }

    @Scheduled(fixedDelayString = "${rydvrse.jobs.tracking-stale-check-delay-ms:60000}")
    public void run() {
        if (!rydvrseProperties.getJobs().isEnabled()) {
            return;
        }
        int marked = trackingSessionService.markStaleSessions();
        if (marked > 0) {
            log.warn("Marked {} tracking sessions as degraded", marked);
        }
    }
}
