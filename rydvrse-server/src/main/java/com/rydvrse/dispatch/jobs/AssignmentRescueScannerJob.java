package com.rydvrse.dispatch.jobs;

import com.rydvrse.common.config.RydvrseProperties;
import com.rydvrse.dispatch.application.RescueQueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AssignmentRescueScannerJob {

    private static final Logger log = LoggerFactory.getLogger(AssignmentRescueScannerJob.class);

    private final RescueQueueService rescueQueueService;
    private final RydvrseProperties rydvrseProperties;

    public AssignmentRescueScannerJob(RescueQueueService rescueQueueService, RydvrseProperties rydvrseProperties) {
        this.rescueQueueService = rescueQueueService;
        this.rydvrseProperties = rydvrseProperties;
    }

    @Scheduled(fixedDelayString = "${rydvrse.jobs.assignment-rescue-scan-delay-ms:45000}")
    public void run() {
        if (!rydvrseProperties.getJobs().isEnabled()) {
            return;
        }
        int flagged = rescueQueueService.scanAndFlagAtRiskAssignments();
        if (flagged > 0) {
            log.info("Flagged {} assignments for rescue review", flagged);
        }
    }
}
