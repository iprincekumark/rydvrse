package com.rydvrse.booking.application;

import com.rydvrse.auth.infrastructure.UserAccountRepository;
import com.rydvrse.booking.domain.BookingEntity;
import com.rydvrse.booking.domain.BookingInstructionEntity;
import com.rydvrse.booking.domain.BookingPassengerContextEntity;
import com.rydvrse.booking.infrastructure.BookingInstructionRepository;
import com.rydvrse.booking.infrastructure.BookingPassengerContextRepository;
import com.rydvrse.booking.infrastructure.BookingRepository;
import com.rydvrse.booking.infrastructure.BookingStateLogRepository;
import com.rydvrse.common.error.ApiException;
import com.rydvrse.common.error.ErrorCode;
import com.rydvrse.customer.application.CustomerProfileService;
import com.rydvrse.dispatch.domain.AssignmentEntity;
import com.rydvrse.dispatch.infrastructure.AssignmentRepository;
import com.rydvrse.pricing.domain.BookingFareSnapshotEntity;
import com.rydvrse.pricing.infrastructure.BookingFareSnapshotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class BookingQueryService {

    private final BookingRepository bookingRepository;
    private final BookingPassengerContextRepository passengerContextRepository;
    private final BookingInstructionRepository bookingInstructionRepository;
    private final BookingStateLogRepository bookingStateLogRepository;
    private final AssignmentRepository assignmentRepository;
    private final BookingFareSnapshotRepository bookingFareSnapshotRepository;
    private final CustomerProfileService customerProfileService;
    private final UserAccountRepository userAccountRepository;

    public BookingQueryService(
            BookingRepository bookingRepository,
            BookingPassengerContextRepository passengerContextRepository,
            BookingInstructionRepository bookingInstructionRepository,
            BookingStateLogRepository bookingStateLogRepository,
            AssignmentRepository assignmentRepository,
            BookingFareSnapshotRepository bookingFareSnapshotRepository,
            CustomerProfileService customerProfileService,
            UserAccountRepository userAccountRepository
    ) {
        this.bookingRepository = bookingRepository;
        this.passengerContextRepository = passengerContextRepository;
        this.bookingInstructionRepository = bookingInstructionRepository;
        this.bookingStateLogRepository = bookingStateLogRepository;
        this.assignmentRepository = assignmentRepository;
        this.bookingFareSnapshotRepository = bookingFareSnapshotRepository;
        this.customerProfileService = customerProfileService;
        this.userAccountRepository = userAccountRepository;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listMine() {
        UUID customerProfileId = customerProfileService.requireCurrentProfile().getId();
        return bookingRepository.findByCustomerProfileIdOrderByScheduledPickupAtDesc(customerProfileId).stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getMine(UUID bookingId) {
        UUID customerProfileId = customerProfileService.requireCurrentProfile().getId();
        BookingEntity booking = bookingRepository.findByIdAndCustomerProfileId(bookingId, customerProfileId)
                .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "Booking not found"));
        return toDetail(booking);
    }

    public Map<String, Object> toSummary(BookingEntity booking) {
        return Map.of(
                "booking_id", booking.getId(),
                "booking_number", booking.getBookingCode(),
                "state", booking.getStatus(),
                "service_type", booking.getServiceType(),
                "scheduled_pickup_at", booking.getScheduledPickupAt(),
                "pickup", Map.of(
                        "address_line_1", booking.getPickupAddressText(),
                        "city_id", booking.getCityId(),
                        "zone_id", booking.getPickupZoneId(),
                        "latitude", booking.getPickupLatitude(),
                        "longitude", booking.getPickupLongitude()
                ),
                "drop", booking.getDropAddressText() == null ? null : Map.of(
                        "address_line_1", booking.getDropAddressText(),
                        "city_id", booking.getCityId(),
                        "zone_id", booking.getDropZoneId(),
                        "latitude", booking.getDropLatitude(),
                        "longitude", booking.getDropLongitude()
                ),
                "total_paise", booking.getCurrentTotalPaise(),
                "payment_state", booking.getPaymentState(),
                "assignment", booking.getCurrentAssignmentId() == null ? null : assignmentRepository.findById(booking.getCurrentAssignmentId()).map(this::toAssignmentSummary).orElse(null)
        );
    }

    public Map<String, Object> toDetail(BookingEntity booking) {
        BookingPassengerContextEntity passengerContext = passengerContextRepository.findByBookingId(booking.getId()).orElse(null);
        BookingFareSnapshotEntity fareSnapshot = bookingFareSnapshotRepository.findByBookingId(booking.getId()).orElse(null);
        List<BookingInstructionEntity> instructions = bookingInstructionRepository.findByBookingIdOrderByCreatedAtAsc(booking.getId());
        List<Map<String, Object>> timeline = bookingStateLogRepository.findByBookingIdOrderByChangedAtAsc(booking.getId()).stream()
                .map(log -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("from_state", log.getFromStatus());
                    row.put("to_state", log.getToStatus());
                    row.put("changed_at", log.getChangedAt());
                    row.put("reason_code", log.getReasonCode());
                    return row;
                })
                .toList();
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("booking_id", booking.getId());
        detail.put("booking_number", booking.getBookingCode());
        detail.put("state", booking.getStatus());
        detail.put("service_type", booking.getServiceType());
        detail.put("scheduled_pickup_at", booking.getScheduledPickupAt());
        detail.put("pickup", Map.of(
                "address_line_1", booking.getPickupAddressText(),
                "city_id", booking.getCityId(),
                "zone_id", booking.getPickupZoneId(),
                "latitude", booking.getPickupLatitude(),
                "longitude", booking.getPickupLongitude()
        ));
        detail.put("drop", booking.getDropAddressText() == null ? null : Map.of(
                "address_line_1", booking.getDropAddressText(),
                "city_id", booking.getCityId(),
                "zone_id", booking.getDropZoneId(),
                "latitude", booking.getDropLatitude(),
                "longitude", booking.getDropLongitude()
        ));
        detail.put("quote_snapshot", fareSnapshot == null ? null : fareSnapshot.getSnapshotPayload());
        detail.put("fare_components", fareSnapshot == null ? List.of() : fareSnapshot.getSnapshotPayload().path("components"));
        detail.put("timeline", timeline);
        detail.put("customer_notes", instructions.stream().map(BookingInstructionEntity::getInstructionText).toList());
        detail.put("trip_start_required", true);
        detail.put("modification_allowed", booking.getStatus().equals("PENDING_ASSIGNMENT"));
        detail.put("cancellation_allowed", List.of("PENDING_ASSIGNMENT", "ASSIGNED", "ARRIVED").contains(booking.getStatus()));
        detail.put("cancellation_preview", null);
        detail.put("support_summary", Map.of("open_ticket_count", 0));
        detail.put("trip_id", booking.getCurrentTripId());
        detail.put("invoice_id", null);
        detail.put("payment_summary", Map.of("state", booking.getPaymentState()));
        detail.put("assignment", booking.getCurrentAssignmentId() == null ? null : assignmentRepository.findById(booking.getCurrentAssignmentId()).map(this::toAssignmentSummary).orElse(null));
        detail.put("passenger", passengerContext == null ? null : Map.of(
                "name", passengerContext.getPassengerName(),
                "mobile_number", passengerContext.getPassengerMobileE164(),
                "is_self_booking", passengerContext.isSelfBooking()
        ));
        return detail;
    }

    private Map<String, Object> toAssignmentSummary(AssignmentEntity assignment) {
        return Map.of(
                "assignment_id", assignment.getId(),
                "state", assignment.getStatus(),
                "driver", null,
                "eta_minutes", assignment.getDriverEtaSeconds() == null ? null : assignment.getDriverEtaSeconds() / 60,
                "assigned_at", assignment.getAssignedAt(),
                "arrived_at", null,
                "reassignment_count", Math.max(0, assignment.getAssignmentSequenceNo() - 1)
        );
    }
}
