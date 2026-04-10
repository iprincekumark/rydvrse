package com.rydvrse.dispatch.application;

import com.rydvrse.booking.domain.BookingEntity;
import com.rydvrse.booking.infrastructure.BookingRepository;
import com.rydvrse.common.config.RydvrseProperties;
import com.rydvrse.common.error.ApiException;
import com.rydvrse.common.error.ErrorCode;
import com.rydvrse.common.idempotency.IdempotencyService;
import com.rydvrse.common.outbox.OutboxService;
import com.rydvrse.dispatch.domain.AssignmentAttemptEntity;
import com.rydvrse.dispatch.domain.AssignmentEntity;
import com.rydvrse.dispatch.infrastructure.AssignmentAttemptRepository;
import com.rydvrse.dispatch.infrastructure.AssignmentRepository;
import com.rydvrse.driver.application.DriverProfileService;
import com.rydvrse.driver.domain.DriverProfileEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AssignmentOfferService {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentAttemptRepository assignmentAttemptRepository;
    private final BookingRepository bookingRepository;
    private final DriverProfileService driverProfileService;
    private final IdempotencyService idempotencyService;
    private final OutboxService outboxService;
    private final RydvrseProperties rydvrseProperties;

    public AssignmentOfferService(
            AssignmentRepository assignmentRepository,
            AssignmentAttemptRepository assignmentAttemptRepository,
            BookingRepository bookingRepository,
            DriverProfileService driverProfileService,
            IdempotencyService idempotencyService,
            OutboxService outboxService,
            RydvrseProperties rydvrseProperties
    ) {
        this.assignmentRepository = assignmentRepository;
        this.assignmentAttemptRepository = assignmentAttemptRepository;
        this.bookingRepository = bookingRepository;
        this.driverProfileService = driverProfileService;
        this.idempotencyService = idempotencyService;
        this.outboxService = outboxService;
        this.rydvrseProperties = rydvrseProperties;
    }

    @Transactional
    public Map<String, Object> declineOffer(UUID assignmentId, String reasonCode, String reasonNote, String idempotencyKey) {
        DriverProfileEntity driver = driverProfileService.requireCurrentProfile();
        AssignmentEntity assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "Assignment not found"));
        if (!driver.getId().equals(assignment.getDriverProfileId())) {
            throw ApiException.forbidden(ErrorCode.FORBIDDEN, "Assignment not visible");
        }
        String scope = "POST:/api/v1/drivers/assignments/" + assignmentId + "/decline";
        return idempotencyService.checkExisting(scope, idempotencyKey, scope)
                .map(existing -> Map.<String, Object>of("assignment_id", assignmentId, "state", "REJECTED"))
                .orElseGet(() -> {
                    assignment.setStatus("REJECTED");
                    assignment.setRiskStatus("AT_RISK");
                    assignment.setRescueRequired(true);
                    assignmentRepository.save(assignment);

                    assignmentAttemptRepository.findFirstByAssignmentIdAndDriverProfileIdOrderByOfferSequenceNoDesc(assignmentId, driver.getId())
                            .ifPresent(attempt -> {
                                attempt.setAttemptStatus("REJECTED");
                                attempt.setRespondedAt(OffsetDateTime.now());
                                attempt.setResponseReasonCode(reasonCode);
                                assignmentAttemptRepository.save(attempt);
                            });

                    BookingEntity booking = bookingRepository.findById(assignment.getBookingId()).orElseThrow();
                    booking.setStatus("PENDING_ASSIGNMENT");
                    bookingRepository.save(booking);

                    outboxService.publish("assignment", assignment.getId(), "AssignmentExpiredEvent", Map.of(
                            "assignment_id", assignment.getId(),
                            "reason_code", reasonCode,
                            "reason_note", reasonNote == null ? "" : reasonNote
                    ));
                    idempotencyService.store(scope, idempotencyKey, scope, "ASSIGNMENT", assignment.getId(), "SUCCEEDED");
                    return Map.of(
                            "assignment_id", assignment.getId(),
                            "state", "REJECTED",
                            "reason_code", reasonCode,
                            "reason_note", reasonNote
                    );
                });
    }

    @Transactional
    public int expireStaleOffers() {
        OffsetDateTime cutoff = OffsetDateTime.now().minusSeconds(Math.max(60, rydvrseProperties.getTracking().getStaleThresholdSeconds() * 3));
        List<AssignmentAttemptEntity> staleAttempts = assignmentAttemptRepository.findByAttemptStatusAndOfferedAtBefore("OFFERED", cutoff);
        int expired = 0;
        for (AssignmentAttemptEntity attempt : staleAttempts) {
            AssignmentEntity assignment = assignmentRepository.findById(attempt.getAssignmentId()).orElse(null);
            if (assignment == null || !"OFFERED".equals(assignment.getStatus())) {
                continue;
            }
            attempt.setAttemptStatus("EXPIRED");
            attempt.setRespondedAt(OffsetDateTime.now());
            assignmentAttemptRepository.save(attempt);

            assignment.setStatus("EXPIRED");
            assignment.setRiskStatus("AT_RISK");
            assignment.setRescueRequired(true);
            assignmentRepository.save(assignment);

            bookingRepository.findById(assignment.getBookingId()).ifPresent(booking -> {
                booking.setStatus("PENDING_ASSIGNMENT");
                bookingRepository.save(booking);
            });
            outboxService.publish("assignment", assignment.getId(), "AssignmentExpiredEvent", Map.of(
                    "assignment_id", assignment.getId(),
                    "reason_code", "AUTO_TIMEOUT"
            ));
            expired++;
        }
        return expired;
    }
}
