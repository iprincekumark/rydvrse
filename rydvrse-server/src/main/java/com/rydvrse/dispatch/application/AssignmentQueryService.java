package com.rydvrse.dispatch.application;

import com.rydvrse.booking.domain.BookingEntity;
import com.rydvrse.booking.infrastructure.BookingRepository;
import com.rydvrse.common.error.ApiException;
import com.rydvrse.common.error.ErrorCode;
import com.rydvrse.dispatch.domain.AssignmentAttemptEntity;
import com.rydvrse.dispatch.domain.AssignmentEntity;
import com.rydvrse.dispatch.domain.DriverCandidateSnapshotEntity;
import com.rydvrse.dispatch.infrastructure.AssignmentAttemptRepository;
import com.rydvrse.dispatch.infrastructure.AssignmentRepository;
import com.rydvrse.dispatch.infrastructure.DriverCandidateSnapshotRepository;
import com.rydvrse.driver.application.DriverProfileService;
import com.rydvrse.driver.domain.DriverProfileEntity;
import com.rydvrse.driver.infrastructure.DriverProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AssignmentQueryService {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentAttemptRepository assignmentAttemptRepository;
    private final DriverCandidateSnapshotRepository driverCandidateSnapshotRepository;
    private final BookingRepository bookingRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final DriverProfileService driverProfileService;

    public AssignmentQueryService(
            AssignmentRepository assignmentRepository,
            AssignmentAttemptRepository assignmentAttemptRepository,
            DriverCandidateSnapshotRepository driverCandidateSnapshotRepository,
            BookingRepository bookingRepository,
            DriverProfileRepository driverProfileRepository,
            DriverProfileService driverProfileService
    ) {
        this.assignmentRepository = assignmentRepository;
        this.assignmentAttemptRepository = assignmentAttemptRepository;
        this.driverCandidateSnapshotRepository = driverCandidateSnapshotRepository;
        this.bookingRepository = bookingRepository;
        this.driverProfileRepository = driverProfileRepository;
        this.driverProfileService = driverProfileService;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> offersForCurrentDriver() {
        UUID driverId = driverProfileService.requireCurrentProfile().getId();
        return assignmentRepository.findByDriverProfileIdAndStatusOrderByUpdatedAtDesc(driverId, "OFFERED").stream()
                .map(this::toAssignmentSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> assignmentDetail(UUID assignmentId) {
        UUID driverId = driverProfileService.requireCurrentProfile().getId();
        AssignmentEntity assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "Assignment not found"));
        if (!driverId.equals(assignment.getDriverProfileId())) {
            throw ApiException.forbidden(ErrorCode.FORBIDDEN, "Assignment not visible");
        }

        BookingEntity booking = bookingRepository.findById(assignment.getBookingId())
                .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "Booking not found"));
        List<AssignmentAttemptEntity> attempts = assignmentAttemptRepository.findByAssignmentIdOrderByOfferSequenceNoAsc(assignmentId);
        List<DriverCandidateSnapshotEntity> candidateSnapshots = driverCandidateSnapshotRepository.findByAssignmentIdOrderBySnapshotRankAsc(assignmentId);

        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("assignment", toAssignmentSummary(assignment));
        detail.put("booking", Map.of(
                "booking_id", booking.getId(),
                "booking_number", booking.getBookingCode(),
                "service_type", booking.getServiceType(),
                "scheduled_pickup_at", booking.getScheduledPickupAt()
        ));
        detail.put("pickup", Map.of(
                "address_line_1", booking.getPickupAddressText(),
                "latitude", booking.getPickupLatitude(),
                "longitude", booking.getPickupLongitude()
        ));
        detail.put("drop", booking.getDropAddressText() == null ? null : Map.of(
                "address_line_1", booking.getDropAddressText(),
                "latitude", booking.getDropLatitude(),
                "longitude", booking.getDropLongitude()
        ));
        detail.put("earning_preview", Map.of(
                "assignment_id", assignment.getId(),
                "total_payout_paise", Math.round(booking.getCurrentTotalPaise() * 0.70)
        ));
        detail.put("attempt_history", attempts.stream().map(attempt -> Map.of(
                "offer_sequence_no", attempt.getOfferSequenceNo(),
                "state", attempt.getAttemptStatus(),
                "offered_at", attempt.getOfferedAt(),
                "responded_at", attempt.getRespondedAt(),
                "reason_code", attempt.getResponseReasonCode()
        )).toList());
        detail.put("candidate_snapshot", candidateSnapshots.stream().map(snapshot -> Map.of(
                "driver_id", snapshot.getDriverProfileId(),
                "rank", snapshot.getSnapshotRank(),
                "score", snapshot.getScore(),
                "snapshot_payload", snapshot.getSnapshotPayload()
        )).toList());
        detail.put("customer_masked_contact", "hidden");
        detail.put("special_instructions", List.of());
        return detail;
    }

    public Map<String, Object> toAssignmentSummary(AssignmentEntity assignment) {
        BookingEntity booking = bookingRepository.findById(assignment.getBookingId()).orElse(null);
        DriverProfileEntity driver = assignment.getDriverProfileId() == null ? null : driverProfileRepository.findById(assignment.getDriverProfileId()).orElse(null);
        return Map.of(
                "assignment_id", assignment.getId(),
                "state", assignment.getStatus(),
                "eta_minutes", assignment.getDriverEtaSeconds() == null ? null : assignment.getDriverEtaSeconds() / 60,
                "assigned_at", assignment.getAssignedAt(),
                "reassignment_count", Math.max(0, assignment.getAssignmentSequenceNo() - 1),
                "earning_preview", Map.of("total_payout_paise", booking == null ? 0 : Math.round(booking.getCurrentTotalPaise() * 0.70)),
                "driver", driver == null ? null : Map.of(
                        "driver_id", driver.getId(),
                        "full_name", (driver.getFirstName() + " " + (driver.getLastName() == null ? "" : driver.getLastName())).trim(),
                        "compliance_status", driver.getComplianceStatus()
                )
        );
    }
}
