package com.rydvrse.admin.application;

import com.rydvrse.booking.application.BookingCancellationService;
import com.rydvrse.booking.application.BookingQueryService;
import com.rydvrse.booking.domain.BookingEntity;
import com.rydvrse.booking.infrastructure.BookingRepository;
import com.rydvrse.common.audit.AuditService;
import com.rydvrse.common.error.ApiException;
import com.rydvrse.common.error.ErrorCode;
import com.rydvrse.common.idempotency.IdempotencyService;
import com.rydvrse.common.outbox.OutboxService;
import com.rydvrse.common.security.ActorType;
import com.rydvrse.common.security.CurrentActorService;
import com.rydvrse.dispatch.domain.AssignmentEntity;
import com.rydvrse.dispatch.infrastructure.AssignmentRepository;
import com.rydvrse.driver.domain.DriverProfileEntity;
import com.rydvrse.driver.infrastructure.DriverProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AdminBookingService {

    private final BookingRepository bookingRepository;
    private final AssignmentRepository assignmentRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final BookingQueryService bookingQueryService;
    private final BookingCancellationService bookingCancellationService;
    private final IdempotencyService idempotencyService;
    private final AuditService auditService;
    private final OutboxService outboxService;
    private final CurrentActorService currentActorService;

    public AdminBookingService(
            BookingRepository bookingRepository,
            AssignmentRepository assignmentRepository,
            DriverProfileRepository driverProfileRepository,
            BookingQueryService bookingQueryService,
            BookingCancellationService bookingCancellationService,
            IdempotencyService idempotencyService,
            AuditService auditService,
            OutboxService outboxService,
            CurrentActorService currentActorService
    ) {
        this.bookingRepository = bookingRepository;
        this.assignmentRepository = assignmentRepository;
        this.driverProfileRepository = driverProfileRepository;
        this.bookingQueryService = bookingQueryService;
        this.bookingCancellationService = bookingCancellationService;
        this.idempotencyService = idempotencyService;
        this.auditService = auditService;
        this.outboxService = outboxService;
        this.currentActorService = currentActorService;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> list() {
        currentActorService.requireActor(ActorType.ADMIN);
        return bookingRepository.findAll().stream()
                .sorted(java.util.Comparator.comparing(BookingEntity::getScheduledPickupAt).reversed())
                .map(bookingQueryService::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> detail(UUID bookingId) {
        currentActorService.requireActor(ActorType.ADMIN);
        BookingEntity booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "Booking not found"));
        return bookingQueryService.toDetail(booking);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> rescueQueue() {
        currentActorService.requireActor(ActorType.ADMIN);
        return bookingRepository.findByStatusInAndScheduledPickupAtBetween(
                        List.of("PENDING_ASSIGNMENT", "ASSIGNED"),
                        OffsetDateTime.now(),
                        OffsetDateTime.now().plusHours(3)
                ).stream()
                .filter(booking -> "PENDING_ASSIGNMENT".equals(booking.getStatus()) || booking.getScheduledPickupAt().isBefore(OffsetDateTime.now().plusMinutes(30)))
                .map(booking -> Map.of(
                        "booking_id", booking.getId(),
                        "booking_number", booking.getBookingCode(),
                        "state", booking.getStatus(),
                        "time_to_sla_breach_seconds", Math.max(0, java.time.Duration.between(OffsetDateTime.now(), booking.getScheduledPickupAt()).toSeconds()),
                        "suggested_rescue_action_metadata", Map.of("action", "MANUAL_REASSIGN")
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> candidates(UUID bookingId) {
        currentActorService.requireActor(ActorType.ADMIN);
        BookingEntity booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "Booking not found"));
        return driverProfileRepository.findAll().stream()
                .filter(driver -> "APPROVED".equals(driver.getOnboardingStatus()))
                .filter(driver -> booking.getCityId().equals(driver.getDefaultCityId()))
                .map(driver -> Map.of(
                        "driver_id", driver.getId(),
                        "full_name", (driver.getFirstName() + " " + (driver.getLastName() == null ? "" : driver.getLastName())).trim(),
                        "eligibility_flags", List.of("CITY_MATCH", "APPROVED"),
                        "distance_to_pickup_meters", 0,
                        "expected_arrival_time_minutes", 15,
                        "trust_summary", Map.of("compliance_status", driver.getComplianceStatus()),
                        "payout_preview_summary", Map.of("amount_paise", Math.round(booking.getCurrentTotalPaise() * 0.70))
                ))
                .toList();
    }

    @Transactional
    public Map<String, Object> reassign(UUID bookingId, ReassignCommand command, String idempotencyKey) {
        currentActorService.requireActor(ActorType.ADMIN);
        String scope = "POST:/api/v1/admin/bookings/" + bookingId + "/reassign";
        return idempotencyService.checkExisting(scope, idempotencyKey, idempotencyService.hashPayload(command))
                .map(existing -> {
                    Map<String, Object> result = new java.util.LinkedHashMap<>();
                    result.put("booking_id", existing.responseReferenceId());
                    result.put("state", "ASSIGNED");
                    return result;
                })
                .orElseGet(() -> {
                    BookingEntity booking = bookingRepository.findById(bookingId)
                            .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "Booking not found"));
                    if (!booking.getRowVersion().equals(command.rowVersion())) {
                        throw ApiException.conflict(ErrorCode.STALE_ROW_VERSION, "Booking has changed");
                    }
                    AssignmentEntity assignment = assignmentRepository.findByBookingIdAndCurrentTrue(bookingId)
                            .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "Assignment not found"));
                    DriverProfileEntity driver = driverProfileRepository.findById(command.targetDriverId())
                            .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "Driver not found"));
                    assignment.setDriverProfileId(driver.getId());
                    assignment.setStatus("OFFERED");
                    assignment.setAssignedAt(OffsetDateTime.now());
                    assignment.setDriverEtaSeconds(15 * 60);
                    assignment.setRiskStatus("NORMAL");
                    assignment.setRescueRequired(false);
                    assignmentRepository.save(assignment);
                    booking.setStatus("ASSIGNED");
                    bookingRepository.save(booking);
                    auditService.record("ASSIGNMENT_REASSIGNED", "BOOKING", bookingId, null, Map.of("driver_id", driver.getId()), command.reasonCode(), command.reasonNote());
                    outboxService.publish("assignment", assignment.getId(), "AssignmentReassignedEvent", Map.of("booking_id", bookingId, "driver_id", driver.getId()));
                    idempotencyService.store(scope, idempotencyKey, command, "BOOKING", bookingId, "SUCCEEDED");
                    return Map.of(
                            "updated_booking_detail", bookingQueryService.toDetail(booking),
                            "new_assignment_summary", Map.of(
                                    "assignment_id", assignment.getId(),
                                    "state", assignment.getStatus(),
                                    "driver_id", driver.getId()
                            ),
                            "audit_reference", "ASSIGNMENT_REASSIGNED"
                    );
                });
    }

    @Transactional
    public Map<String, Object> adminCancel(UUID bookingId, AdminCancelCommand command, String idempotencyKey) {
        currentActorService.requireActor(ActorType.ADMIN);
        BookingEntity booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "Booking not found"));
        if (!booking.getRowVersion().equals(command.rowVersion())) {
            throw ApiException.conflict(ErrorCode.STALE_ROW_VERSION, "Booking has changed");
        }
        return bookingCancellationService.cancel(
                bookingId,
                new BookingCancellationService.CancelBookingCommand(command.reasonCode(), command.reasonNote()),
                idempotencyKey
        );
    }

    public record ReassignCommand(UUID targetDriverId, String reasonCode, String reasonNote, Long rowVersion, boolean overrideAcknowledged) {
    }

    public record AdminCancelCommand(String reasonCode, String reasonNote, String refundRecommendation, Long rowVersion) {
    }
}
