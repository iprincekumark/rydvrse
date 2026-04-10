package com.rydvrse.dispatch.application;

import com.rydvrse.booking.domain.BookingEntity;
import com.rydvrse.booking.infrastructure.BookingRepository;
import com.rydvrse.dispatch.domain.AssignmentEntity;
import com.rydvrse.dispatch.infrastructure.AssignmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class RescueQueueService {

    private final BookingRepository bookingRepository;
    private final AssignmentRepository assignmentRepository;
    private final CandidateDiscoveryService candidateDiscoveryService;

    public RescueQueueService(
            BookingRepository bookingRepository,
            AssignmentRepository assignmentRepository,
            CandidateDiscoveryService candidateDiscoveryService
    ) {
        this.bookingRepository = bookingRepository;
        this.assignmentRepository = assignmentRepository;
        this.candidateDiscoveryService = candidateDiscoveryService;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> rescueQueue() {
        return bookingRepository.findByStatusInAndScheduledPickupAtBetween(
                        List.of("PENDING_ASSIGNMENT", "ASSIGNED"),
                        OffsetDateTime.now().minusMinutes(30),
                        OffsetDateTime.now().plusHours(3)
                ).stream()
                .map(booking -> Map.of(
                        "booking_id", booking.getId(),
                        "booking_number", booking.getBookingCode(),
                        "state", booking.getStatus(),
                        "current_assignment_id", booking.getCurrentAssignmentId(),
                        "time_to_sla_breach_seconds", Math.max(0, Duration.between(OffsetDateTime.now(), booking.getScheduledPickupAt()).toSeconds()),
                        "needs_manual_intervention", needsManualIntervention(booking),
                        "suggested_rescue_action_metadata", Map.of("action", "MANUAL_REASSIGN")
                ))
                .filter(row -> Boolean.TRUE.equals(row.get("needs_manual_intervention")))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> assignmentCandidates(UUID bookingId) {
        BookingEntity booking = bookingRepository.findById(bookingId).orElseThrow();
        return candidateDiscoveryService.discover(booking, 10).stream()
                .map(candidate -> Map.<String, Object>of(
                        "driver_id", candidate.driverId(),
                        "full_name", candidate.fullName(),
                        "availability_state", candidate.availabilityState(),
                        "distance_to_pickup_meters", 0,
                        "expected_arrival_time_minutes", candidate.etaMinutes(),
                        "eligibility_flags", List.of("CITY_MATCH", "APPROVED"),
                        "trust_summary", Map.of("score", candidate.score()),
                        "payout_preview_summary", Map.of("amount_paise", Math.round(booking.getCurrentTotalPaise() * 0.70))
                ))
                .toList();
    }

    @Transactional
    public int scanAndFlagAtRiskAssignments() {
        int flagged = 0;
        for (AssignmentEntity assignment : assignmentRepository.findByStatusInOrderByUpdatedAtDesc(List.of("OFFERED", "UNASSIGNED"))) {
            BookingEntity booking = bookingRepository.findById(assignment.getBookingId()).orElse(null);
            if (booking == null) {
                continue;
            }
            boolean atRisk = assignment.isRescueRequired()
                    || "UNASSIGNED".equals(assignment.getStatus())
                    || booking.getScheduledPickupAt().isBefore(OffsetDateTime.now().plusMinutes(20));
            if (atRisk && !assignment.isRescueRequired()) {
                assignment.setRiskStatus("AT_RISK");
                assignment.setRescueRequired(true);
                assignmentRepository.save(assignment);
                flagged++;
            }
        }
        return flagged;
    }

    private boolean needsManualIntervention(BookingEntity booking) {
        if ("PENDING_ASSIGNMENT".equals(booking.getStatus())) {
            return true;
        }
        if (booking.getCurrentAssignmentId() == null) {
            return true;
        }
        return assignmentRepository.findById(booking.getCurrentAssignmentId())
                .map(assignment -> assignment.isRescueRequired() || "UNASSIGNED".equals(assignment.getStatus()))
                .orElse(true);
    }
}
