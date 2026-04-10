package com.rydvrse.dispatch.application;

import com.rydvrse.booking.domain.BookingEntity;
import com.rydvrse.booking.infrastructure.BookingRepository;
import com.rydvrse.common.error.ApiException;
import com.rydvrse.common.error.ErrorCode;
import com.rydvrse.common.idempotency.IdempotencyService;
import com.rydvrse.common.outbox.OutboxService;
import com.rydvrse.common.persistence.JsonNodeUtils;
import com.rydvrse.dispatch.domain.AssignmentAttemptEntity;
import com.rydvrse.dispatch.domain.AssignmentEntity;
import com.rydvrse.dispatch.infrastructure.AssignmentAttemptRepository;
import com.rydvrse.dispatch.infrastructure.AssignmentRepository;
import com.rydvrse.driver.application.DriverProfileService;
import com.rydvrse.driver.domain.DriverAvailabilityStatusEntity;
import com.rydvrse.driver.domain.DriverProfileEntity;
import com.rydvrse.driver.infrastructure.DriverAvailabilityStatusRepository;
import com.rydvrse.tracking.application.TrackingSessionService;
import com.rydvrse.trip.domain.TripEntity;
import com.rydvrse.trip.domain.TripEventEntity;
import com.rydvrse.trip.infrastructure.TripEventRepository;
import com.rydvrse.trip.infrastructure.TripRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class AssignmentLockService {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentAttemptRepository assignmentAttemptRepository;
    private final BookingRepository bookingRepository;
    private final TripRepository tripRepository;
    private final TripEventRepository tripEventRepository;
    private final DriverAvailabilityStatusRepository driverAvailabilityStatusRepository;
    private final DriverProfileService driverProfileService;
    private final TrackingSessionService trackingSessionService;
    private final IdempotencyService idempotencyService;
    private final JsonNodeUtils jsonNodeUtils;
    private final OutboxService outboxService;

    public AssignmentLockService(
            AssignmentRepository assignmentRepository,
            AssignmentAttemptRepository assignmentAttemptRepository,
            BookingRepository bookingRepository,
            TripRepository tripRepository,
            TripEventRepository tripEventRepository,
            DriverAvailabilityStatusRepository driverAvailabilityStatusRepository,
            DriverProfileService driverProfileService,
            TrackingSessionService trackingSessionService,
            IdempotencyService idempotencyService,
            JsonNodeUtils jsonNodeUtils,
            OutboxService outboxService
    ) {
        this.assignmentRepository = assignmentRepository;
        this.assignmentAttemptRepository = assignmentAttemptRepository;
        this.bookingRepository = bookingRepository;
        this.tripRepository = tripRepository;
        this.tripEventRepository = tripEventRepository;
        this.driverAvailabilityStatusRepository = driverAvailabilityStatusRepository;
        this.driverProfileService = driverProfileService;
        this.trackingSessionService = trackingSessionService;
        this.idempotencyService = idempotencyService;
        this.jsonNodeUtils = jsonNodeUtils;
        this.outboxService = outboxService;
    }

    @Transactional
    public Map<String, Object> acceptOffer(UUID assignmentId, String idempotencyKey) {
        DriverProfileEntity driver = driverProfileService.requireCurrentProfile();
        if (!"APPROVED".equals(driver.getOnboardingStatus())) {
            throw ApiException.unprocessable(ErrorCode.DRIVER_NOT_APPROVED, "Driver is not approved for assignments");
        }
        AssignmentEntity assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "Assignment not found"));
        if (!driver.getId().equals(assignment.getDriverProfileId())) {
            throw ApiException.forbidden(ErrorCode.FORBIDDEN, "Assignment not visible");
        }
        if (!"OFFERED".equals(assignment.getStatus())) {
            throw ApiException.unprocessable(ErrorCode.ASSIGNMENT_OFFER_EXPIRED, "Assignment is no longer actionable");
        }
        String scope = "POST:/api/v1/drivers/assignments/" + assignmentId + "/accept";
        return idempotencyService.checkExisting(scope, idempotencyKey, scope)
                .map(existing -> Map.<String, Object>of("assignment_id", existing.responseReferenceId(), "state", "ACCEPTED"))
                .orElseGet(() -> {
                    BookingEntity booking = bookingRepository.findById(assignment.getBookingId()).orElseThrow();
                    assignment.setStatus("ACCEPTED");
                    assignment.setAssignedAt(OffsetDateTime.now());
                    assignment.setRiskStatus("NORMAL");
                    assignment.setRescueRequired(false);
                    assignmentRepository.save(assignment);

                    assignmentAttemptRepository.findFirstByAssignmentIdAndDriverProfileIdOrderByOfferSequenceNoDesc(assignmentId, driver.getId())
                            .ifPresent(attempt -> {
                                attempt.setAttemptStatus("ACCEPTED");
                                attempt.setRespondedAt(OffsetDateTime.now());
                                assignmentAttemptRepository.save(attempt);
                            });

                    booking.setStatus("ASSIGNED");
                    booking.setCurrentAssignmentId(assignment.getId());
                    bookingRepository.save(booking);

                    TripEntity trip = tripRepository.findByBookingId(booking.getId()).orElseGet(() -> {
                        TripEntity entity = new TripEntity();
                        entity.setBookingId(booking.getId());
                        entity.setAssignmentId(assignment.getId());
                        entity.setDriverProfileId(driver.getId());
                        entity.setCustomerProfileId(booking.getCustomerProfileId());
                        entity.setStatus("ARRIVAL_PENDING");
                        return tripRepository.save(entity);
                    });
                    booking.setCurrentTripId(trip.getId());
                    bookingRepository.save(booking);

                    trackingSessionService.openForTrip(trip.getId());
                    driverAvailabilityStatusRepository.findById(driver.getId()).ifPresent(status -> updateAvailability(status, assignment, booking, trip));
                    appendTripEvent(trip.getId(), "assignment.accepted", Map.of("assignment_id", assignmentId));
                    outboxService.publish("assignment", assignment.getId(), "AssignmentLockedEvent", Map.of(
                            "assignment_id", assignment.getId(),
                            "booking_id", booking.getId(),
                            "trip_id", trip.getId()
                    ));
                    idempotencyService.store(scope, idempotencyKey, scope, "ASSIGNMENT", assignment.getId(), "SUCCEEDED");
                    return Map.of(
                            "assignment_id", assignment.getId(),
                            "state", assignment.getStatus(),
                            "trip_id", trip.getId()
                    );
                });
    }

    private void updateAvailability(
            DriverAvailabilityStatusEntity status,
            AssignmentEntity assignment,
            BookingEntity booking,
            TripEntity trip
    ) {
        status.setCurrentStatus("RESERVED");
        status.setCurrentAssignmentId(assignment.getId());
        status.setCurrentBookingId(booking.getId());
        status.setCurrentTripId(trip.getId());
        driverAvailabilityStatusRepository.save(status);
    }

    private void appendTripEvent(UUID tripId, String eventType, Map<String, Object> payload) {
        TripEventEntity event = new TripEventEntity();
        event.setTripId(tripId);
        event.setEventType(eventType);
        event.setEventAt(OffsetDateTime.now());
        event.setActorType("DRIVER");
        event.setEventPayload(jsonNodeUtils.toJsonNode(payload));
        tripEventRepository.save(event);
    }
}
