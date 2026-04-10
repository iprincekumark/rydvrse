package com.rydvrse.dispatch.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rydvrse.booking.domain.BookingEntity;
import com.rydvrse.booking.infrastructure.BookingRepository;
import com.rydvrse.common.outbox.OutboxService;
import com.rydvrse.common.persistence.JsonNodeUtils;
import com.rydvrse.dispatch.domain.AssignmentAttemptEntity;
import com.rydvrse.dispatch.domain.AssignmentEntity;
import com.rydvrse.dispatch.domain.DriverCandidateSnapshotEntity;
import com.rydvrse.dispatch.infrastructure.AssignmentAttemptRepository;
import com.rydvrse.dispatch.infrastructure.AssignmentRepository;
import com.rydvrse.dispatch.infrastructure.DriverCandidateSnapshotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DispatchOrchestratorTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private AssignmentAttemptRepository assignmentAttemptRepository;

    @Mock
    private DriverCandidateSnapshotRepository driverCandidateSnapshotRepository;

    @Mock
    private CandidateDiscoveryService candidateDiscoveryService;

    @Mock
    private OutboxService outboxService;

    private final JsonNodeUtils jsonNodeUtils = new JsonNodeUtils(new ObjectMapper());

    @Test
    void orchestrateShouldCreateOfferForTopCandidate() {
        DispatchOrchestrator orchestrator = new DispatchOrchestrator(
                bookingRepository,
                assignmentRepository,
                assignmentAttemptRepository,
                driverCandidateSnapshotRepository,
                candidateDiscoveryService,
                jsonNodeUtils,
                outboxService
        );

        UUID bookingId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();
        BookingEntity booking = new BookingEntity();
        booking.setId(bookingId);
        booking.setCityId(UUID.randomUUID());
        booking.setServiceType("SCHEDULED_LOCAL");
        booking.setStatus("PENDING_ASSIGNMENT");
        booking.setScheduledPickupAt(OffsetDateTime.now().plusHours(2));

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(assignmentRepository.findByBookingIdAndCurrentTrue(bookingId)).thenReturn(Optional.empty());
        when(candidateDiscoveryService.discover(booking, 10)).thenReturn(List.of(
                new CandidateDiscoveryService.Candidate(driverId, "Driver One", "ONLINE", java.math.BigDecimal.valueOf(92.5), 8)
        ));

        AtomicInteger idSeed = new AtomicInteger(1);
        doAnswer(invocation -> {
            AssignmentEntity entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(new UUID(0L, idSeed.getAndIncrement()));
            }
            return entity;
        }).when(assignmentRepository).save(any(AssignmentEntity.class));
        doAnswer(invocation -> invocation.getArgument(0)).when(assignmentAttemptRepository).save(any(AssignmentAttemptEntity.class));
        doAnswer(invocation -> invocation.getArgument(0)).when(driverCandidateSnapshotRepository).save(any(DriverCandidateSnapshotEntity.class));
        doAnswer(invocation -> invocation.getArgument(0)).when(bookingRepository).save(any(BookingEntity.class));

        var response = orchestrator.orchestrate(bookingId, "BookingConfirmedEvent");

        assertThat(response.get("dispatch_state")).isEqualTo("OFFERED");
        assertThat(booking.getStatus()).isEqualTo("ASSIGNED");
        assertThat(booking.getCurrentAssignmentId()).isNotNull();
        verify(outboxService).publish(any(), any(), any(), any());
    }
}
