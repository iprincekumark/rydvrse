package com.rydvrse.reporting.application;

import com.rydvrse.common.security.ActorType;
import com.rydvrse.common.security.CurrentActorService;
import com.rydvrse.reporting.infrastructure.ReportingJdbcRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Service
public class OperationsReportingService {

    private final ReportingJdbcRepository reportingJdbcRepository;
    private final CurrentActorService currentActorService;

    public OperationsReportingService(ReportingJdbcRepository reportingJdbcRepository, CurrentActorService currentActorService) {
        this.reportingJdbcRepository = reportingJdbcRepository;
        this.currentActorService = currentActorService;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> summary(LocalDate fromDate, LocalDate toDate, UUID cityId) {
        currentActorService.requireActor(ActorType.ADMIN);
        LocalDate resolvedTo = toDate == null ? LocalDate.now() : toDate;
        LocalDate resolvedFrom = fromDate == null ? resolvedTo.minusDays(6) : fromDate;
        return reportingJdbcRepository.operationsSummary(resolvedFrom, resolvedTo, cityId);
    }
}
