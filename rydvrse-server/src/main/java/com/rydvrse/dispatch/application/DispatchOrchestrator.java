package com.rydvrse.dispatch.application;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DispatchOrchestrator {

    private final BookingRepository bookingRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentAttemptRepository assignmentAttemptRepository;
    private final DriverCandidateSnapshotRepository driverCandidateSnapshotRepository;
    private final CandidateDiscoveryService candidateDiscoveryService;
    private final JsonNodeUtils jsonNodeUtils;
    private final OutboxService outboxService;

    public DispatchOrchestrator(
            BookingRepository bookingRepository,
            AssignmentRepository assignmentRepository,
            AssignmentAttemptRepository assignmentAttemptRepository,
            DriverCandidateSnapshotRepository driverCandidateSnapshotRepository,
            CandidateDiscoveryService candidateDiscoveryService,
            JsonNodeUtils jsonNodeUtils,
            OutboxService outboxService
    ) {
        this.bookingRepository = bookingRepository;
        this.assignmentRepository = assignmentRepository;
        this.assignmentAttemptRepository = assignmentAttemptRepository;
        this.driverCandidateSnapshotRepository = driverCandidateSnapshotRepository;
        this.candidateDiscoveryService = candidateDiscoveryService;
        this.jsonNodeUtils = jsonNodeUtils;
        this.outboxService = outboxService;
    }

    @Transactional
    public Map<String, Object> orchestrate(UUID bookingId, String trigger) {
        BookingEntity booking = bookingRepository.findById(bookingId).orElseThrow();
        if (List.of("CANCELLED", "COMPLETED", "IN_PROGRESS").contains(booking.getStatus())) {
            return Map.of("booking_id", bookingId, "dispatch_state", "SKIPPED");
        }

        assignmentRepository.findByBookingIdAndCurrentTrue(bookingId).ifPresent(existing -> {
            existing.setCurrent(false);
            assignmentRepository.save(existing);
        });

        AssignmentEntity assignment = new AssignmentEntity();
        assignment.setBookingId(bookingId);
        assignment.setAssignmentSequenceNo(nextSequence(bookingId));
        assignment.setCurrent(true);
        assignment.setStatus("SEARCHING");
        assignment.setRiskStatus("NORMAL");
        assignment.setRescueRequired(false);
        assignment.setAssignedAt(OffsetDateTime.now());
        assignment = assignmentRepository.save(assignment);

        List<CandidateDiscoveryService.Candidate> candidates = candidateDiscoveryService.discover(booking, 10);
        saveCandidateSnapshots(assignment.getId(), candidates);

        if (candidates.isEmpty()) {
            assignment.setRiskStatus("AT_RISK");
            assignment.setRescueRequired(true);
            assignment.setStatus("UNASSIGNED");
            assignmentRepository.save(assignment);
            booking.setStatus("PENDING_ASSIGNMENT");
            booking.setCurrentAssignmentId(assignment.getId());
            bookingRepository.save(booking);
            outboxService.publish("assignment", assignment.getId(), "AssignmentRescueTriggeredEvent", Map.of(
                    "assignment_id", assignment.getId(),
                    "booking_id", bookingId,
                    "trigger", trigger
            ));
            return Map.of("booking_id", bookingId, "dispatch_state", "NO_CANDIDATES", "assignment_id", assignment.getId());
        }

        CandidateDiscoveryService.Candidate topCandidate = candidates.getFirst();
        assignment.setDriverProfileId(topCandidate.driverId());
        assignment.setStatus("OFFERED");
        assignment.setDriverEtaSeconds(topCandidate.etaMinutes() * 60);
        assignmentRepository.save(assignment);

        AssignmentAttemptEntity attempt = new AssignmentAttemptEntity();
        attempt.setAssignmentId(assignment.getId());
        attempt.setDriverProfileId(topCandidate.driverId());
        attempt.setAttemptStatus("OFFERED");
        attempt.setOfferSequenceNo(1);
        attempt.setOfferedAt(OffsetDateTime.now());
        attempt.setCandidateScore(topCandidate.score());
        attempt.setCandidateSnapshot(jsonNodeUtils.toJsonNode(Map.of(
                "driver_id", topCandidate.driverId(),
                "full_name", topCandidate.fullName(),
                "availability_state", topCandidate.availabilityState(),
                "eta_minutes", topCandidate.etaMinutes(),
                "score", topCandidate.score()
        )));
        assignmentAttemptRepository.save(attempt);

        booking.setCurrentAssignmentId(assignment.getId());
        booking.setStatus("ASSIGNED");
        bookingRepository.save(booking);

        outboxService.publish("assignment", assignment.getId(), "AssignmentOfferCreatedEvent", Map.of(
                "assignment_id", assignment.getId(),
                "booking_id", bookingId,
                "driver_id", topCandidate.driverId(),
                "trigger", trigger
        ));

        return Map.of(
                "booking_id", bookingId,
                "dispatch_state", "OFFERED",
                "assignment_id", assignment.getId(),
                "driver_id", topCandidate.driverId()
        );
    }

    private int nextSequence(UUID bookingId) {
        return assignmentRepository.findByBookingIdAndCurrentTrue(bookingId)
                .map(existing -> existing.getAssignmentSequenceNo() + 1)
                .orElse(1);
    }

    private void saveCandidateSnapshots(UUID assignmentId, List<CandidateDiscoveryService.Candidate> candidates) {
        int rank = 1;
        for (CandidateDiscoveryService.Candidate candidate : candidates) {
            DriverCandidateSnapshotEntity snapshot = new DriverCandidateSnapshotEntity();
            snapshot.setAssignmentId(assignmentId);
            snapshot.setDriverProfileId(candidate.driverId());
            snapshot.setSnapshotRank(rank++);
            snapshot.setScore(candidate.score());
            snapshot.setSnapshotPayload(jsonNodeUtils.toJsonNode(Map.of(
                    "driver_id", candidate.driverId(),
                    "full_name", candidate.fullName(),
                    "availability_state", candidate.availabilityState(),
                    "eta_minutes", candidate.etaMinutes(),
                    "score", candidate.score()
            )));
            driverCandidateSnapshotRepository.save(snapshot);
        }
    }
}
