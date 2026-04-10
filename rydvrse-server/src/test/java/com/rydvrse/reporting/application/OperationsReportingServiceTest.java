package com.rydvrse.reporting.application;

import com.rydvrse.common.security.ActorType;
import com.rydvrse.common.security.CurrentActorService;
import com.rydvrse.reporting.infrastructure.ReportingJdbcRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperationsReportingServiceTest {

    @Mock
    private ReportingJdbcRepository reportingJdbcRepository;

    @Mock
    private CurrentActorService currentActorService;

    @Test
    void summaryShouldDelegateToRepositoryWithResolvedDates() {
        OperationsReportingService service = new OperationsReportingService(reportingJdbcRepository, currentActorService);
        LocalDate from = LocalDate.of(2026, 4, 1);
        LocalDate to = LocalDate.of(2026, 4, 10);
        UUID cityId = UUID.randomUUID();

        when(reportingJdbcRepository.operationsSummary(from, to, cityId)).thenReturn(Map.of("booking_count", 10L));

        Map<String, Object> result = service.summary(from, to, cityId);

        assertThat(result).containsEntry("booking_count", 10L);
        verify(currentActorService).requireActor(ActorType.ADMIN);
        verify(reportingJdbcRepository).operationsSummary(from, to, cityId);
    }
}
