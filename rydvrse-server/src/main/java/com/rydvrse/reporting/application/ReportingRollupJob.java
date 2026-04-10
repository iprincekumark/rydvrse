package com.rydvrse.reporting.application;

import com.rydvrse.common.config.RydvrseProperties;
import com.rydvrse.reporting.infrastructure.ReportingJdbcRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ReportingRollupJob {

    private static final Logger log = LoggerFactory.getLogger(ReportingRollupJob.class);

    private final ReportingJdbcRepository reportingJdbcRepository;
    private final RydvrseProperties rydvrseProperties;

    public ReportingRollupJob(ReportingJdbcRepository reportingJdbcRepository, RydvrseProperties rydvrseProperties) {
        this.reportingJdbcRepository = reportingJdbcRepository;
        this.rydvrseProperties = rydvrseProperties;
    }

    @Scheduled(fixedDelayString = "${rydvrse.jobs.reporting-rollup-delay-ms:300000}")
    public void run() {
        if (!rydvrseProperties.getJobs().isEnabled()) {
            return;
        }
        LocalDate factDate = LocalDate.now();
        reportingJdbcRepository.refreshDailyFacts(factDate);
        log.info("Refreshed reporting rollups for {}", factDate);
    }
}
