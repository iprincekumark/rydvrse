package com.rydvrse.dispatch.jobs;

import com.rydvrse.common.config.RydvrseProperties;
import com.rydvrse.dispatch.application.AssignmentOfferService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AssignmentOfferExpiryJob {

    private static final Logger log = LoggerFactory.getLogger(AssignmentOfferExpiryJob.class);

    private final AssignmentOfferService assignmentOfferService;
    private final RydvrseProperties rydvrseProperties;

    public AssignmentOfferExpiryJob(AssignmentOfferService assignmentOfferService, RydvrseProperties rydvrseProperties) {
        this.assignmentOfferService = assignmentOfferService;
        this.rydvrseProperties = rydvrseProperties;
    }

    @Scheduled(fixedDelayString = "${rydvrse.jobs.assignment-offer-expiry-delay-ms:30000}")
    public void run() {
        if (!rydvrseProperties.getJobs().isEnabled()) {
            return;
        }
        int expired = assignmentOfferService.expireStaleOffers();
        if (expired > 0) {
            log.info("Expired {} stale assignment offers", expired);
        }
    }
}
