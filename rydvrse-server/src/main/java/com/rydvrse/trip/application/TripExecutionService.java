package com.rydvrse.trip.application;

import com.rydvrse.booking.domain.BookingEntity;
import com.rydvrse.booking.infrastructure.BookingRepository;
import com.rydvrse.common.config.RydvrseProperties;
import com.rydvrse.common.error.ApiException;
import com.rydvrse.common.error.ErrorCode;
import com.rydvrse.common.idempotency.IdempotencyService;
import com.rydvrse.common.outbox.OutboxService;
import com.rydvrse.common.persistence.JsonNodeUtils;
import com.rydvrse.common.security.ActorType;
import com.rydvrse.common.security.CurrentActorService;
import com.rydvrse.common.util.HashingUtils;
import com.rydvrse.customer.application.CustomerProfileService;
import com.rydvrse.dispatch.domain.AssignmentEntity;
import com.rydvrse.dispatch.infrastructure.AssignmentRepository;
import com.rydvrse.driver.application.DriverProfileService;
import com.rydvrse.driver.domain.DriverProfileEntity;
import com.rydvrse.driver.infrastructure.DriverAvailabilityStatusRepository;
import com.rydvrse.finance.domain.DriverEarningLedgerEntity;
import com.rydvrse.finance.domain.InvoiceEntity;
import com.rydvrse.finance.domain.PaymentOrderEntity;
import com.rydvrse.finance.infrastructure.DriverEarningLedgerRepository;
import com.rydvrse.finance.infrastructure.InvoiceRepository;
import com.rydvrse.finance.infrastructure.PaymentOrderRepository;
import com.rydvrse.support.application.SupportTicketService;
import com.rydvrse.trip.domain.HandoverChecklistEntity;
import com.rydvrse.trip.domain.LocationPingEntity;
import com.rydvrse.trip.domain.TrackingSessionEntity;
import com.rydvrse.trip.domain.TripEntity;
import com.rydvrse.trip.domain.TripEventEntity;
import com.rydvrse.trip.domain.TripShareLinkEntity;
import com.rydvrse.trip.infrastructure.HandoverChecklistRepository;
import com.rydvrse.trip.infrastructure.LocationPingRepository;
import com.rydvrse.trip.infrastructure.TrackingSessionRepository;
import com.rydvrse.trip.infrastructure.TripEventRepository;
import com.rydvrse.trip.infrastructure.TripRepository;
import com.rydvrse.trip.infrastructure.TripShareLinkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TripExecutionService {

    private final AssignmentRepository assignmentRepository;
    private final BookingRepository bookingRepository;
    private final TripRepository tripRepository;
    private final TripEventRepository tripEventRepository;
    private final HandoverChecklistRepository handoverChecklistRepository;
    private final TrackingSessionRepository trackingSessionRepository;
    private final LocationPingRepository locationPingRepository;
    private final TripShareLinkRepository tripShareLinkRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final InvoiceRepository invoiceRepository;
    private final DriverEarningLedgerRepository driverEarningLedgerRepository;
    private final CurrentActorService currentActorService;
    private final DriverProfileService driverProfileService;
    private final CustomerProfileService customerProfileService;
    private final DriverAvailabilityStatusRepository driverAvailabilityStatusRepository;
    private final IdempotencyService idempotencyService;
    private final JsonNodeUtils jsonNodeUtils;
    private final OutboxService outboxService;
    private final RydvrseProperties rydvrseProperties;
    private final SupportTicketService supportTicketService;

    public TripExecutionService(
            AssignmentRepository assignmentRepository,
            BookingRepository bookingRepository,
            TripRepository tripRepository,
            TripEventRepository tripEventRepository,
            HandoverChecklistRepository handoverChecklistRepository,
            TrackingSessionRepository trackingSessionRepository,
            LocationPingRepository locationPingRepository,
            TripShareLinkRepository tripShareLinkRepository,
            PaymentOrderRepository paymentOrderRepository,
            InvoiceRepository invoiceRepository,
            DriverEarningLedgerRepository driverEarningLedgerRepository,
            CurrentActorService currentActorService,
            DriverProfileService driverProfileService,
            CustomerProfileService customerProfileService,
            DriverAvailabilityStatusRepository driverAvailabilityStatusRepository,
            IdempotencyService idempotencyService,
            JsonNodeUtils jsonNodeUtils,
            OutboxService outboxService,
            RydvrseProperties rydvrseProperties,
            SupportTicketService supportTicketService
    ) {
        this.assignmentRepository = assignmentRepository;
        this.bookingRepository = bookingRepository;
        this.tripRepository = tripRepository;
        this.tripEventRepository = tripEventRepository;
        this.handoverChecklistRepository = handoverChecklistRepository;
        this.trackingSessionRepository = trackingSessionRepository;
        this.locationPingRepository = locationPingRepository;
        this.tripShareLinkRepository = tripShareLinkRepository;
        this.paymentOrderRepository = paymentOrderRepository;
        this.invoiceRepository = invoiceRepository;
        this.driverEarningLedgerRepository = driverEarningLedgerRepository;
        this.currentActorService = currentActorService;
        this.driverProfileService = driverProfileService;
        this.customerProfileService = customerProfileService;
        this.driverAvailabilityStatusRepository = driverAvailabilityStatusRepository;
        this.idempotencyService = idempotencyService;
        this.jsonNodeUtils = jsonNodeUtils;
        this.outboxService = outboxService;
        this.rydvrseProperties = rydvrseProperties;
        this.supportTicketService = supportTicketService;
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
        detail.put("customer_masked_contact", "hidden");
        detail.put("special_instructions", List.of());
        return detail;
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
                    assignmentRepository.save(assignment);
                    booking.setStatus("ASSIGNED");
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
                    TrackingSessionEntity trackingSession = trackingSessionRepository.findByTripId(trip.getId()).orElseGet(() -> {
                        TrackingSessionEntity session = new TrackingSessionEntity();
                        session.setTripId(trip.getId());
                        session.setStatus("ACTIVE");
                        session.setStartedAt(OffsetDateTime.now());
                        return trackingSessionRepository.save(session);
                    });
                    driverAvailabilityStatusRepository.findById(driver.getId()).ifPresent(status -> {
                        status.setCurrentStatus("RESERVED");
                        status.setCurrentAssignmentId(assignment.getId());
                        status.setCurrentBookingId(booking.getId());
                        status.setCurrentTripId(trip.getId());
                        driverAvailabilityStatusRepository.save(status);
                    });
                    appendTripEvent(trip.getId(), "assignment.accepted", Map.of("assignment_id", assignmentId));
                    outboxService.publish("assignment", assignment.getId(), "AssignmentLockedEvent", Map.of("assignment_id", assignment.getId(), "trip_id", trip.getId()));
                    idempotencyService.store(scope, idempotencyKey, scope, "ASSIGNMENT", assignment.getId(), "SUCCEEDED");
                    return Map.of(
                            "assignment_id", assignment.getId(),
                            "state", assignment.getStatus(),
                            "trip_id", trip.getId(),
                            "tracking_session_id", trackingSession.getId()
                    );
                });
    }

    @Transactional
    public Map<String, Object> declineOffer(UUID assignmentId, String reasonCode, String reasonNote, String idempotencyKey) {
        DriverProfileEntity driver = driverProfileService.requireCurrentProfile();
        AssignmentEntity assignment = assignmentRepository.findById(assignmentId).orElseThrow();
        if (!driver.getId().equals(assignment.getDriverProfileId())) {
            throw ApiException.forbidden(ErrorCode.FORBIDDEN, "Assignment not visible");
        }
        String scope = "POST:/api/v1/drivers/assignments/" + assignmentId + "/decline";
        return idempotencyService.checkExisting(scope, idempotencyKey, scope)
                .map(existing -> Map.<String, Object>of("assignment_id", assignmentId, "state", "REJECTED"))
                .orElseGet(() -> {
                    assignment.setStatus("REJECTED");
                    assignmentRepository.save(assignment);
                    outboxService.publish("assignment", assignment.getId(), "AssignmentExpiredEvent", Map.of("assignment_id", assignment.getId(), "reason_code", reasonCode));
                    idempotencyService.store(scope, idempotencyKey, scope, "ASSIGNMENT", assignment.getId(), "SUCCEEDED");
                    return Map.of("assignment_id", assignment.getId(), "state", "REJECTED", "reason_code", reasonCode, "reason_note", reasonNote);
                });
    }

    @Transactional
    public Map<String, Object> markArrived(UUID tripId) {
        TripEntity trip = requireDriverTrip(tripId);
        BookingEntity booking = bookingRepository.findById(trip.getBookingId()).orElseThrow();
        trip.setArrivalMarkedAt(OffsetDateTime.now());
        trip.setStatus("START_PENDING");
        tripRepository.save(trip);
        booking.setStatus("TRIP_START_PENDING");
        bookingRepository.save(booking);
        appendTripEvent(tripId, "trip.arrived", Map.of());
        return tripSummary(trip);
    }

    @Transactional
    public Map<String, Object> confirmStart(UUID bookingId, boolean confirmStart, String fuelNote, String damageNote, String idempotencyKey) {
        UUID customerId = customerProfileService.requireCurrentProfile().getId();
        BookingEntity booking = bookingRepository.findByIdAndCustomerProfileId(bookingId, customerId)
                .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "Booking not found"));
        if (booking.getCurrentTripId() == null) {
            throw ApiException.unprocessable(ErrorCode.TRIP_START_CONFIRMATION_NOT_ALLOWED, "No active trip");
        }
        TripEntity trip = tripRepository.findById(booking.getCurrentTripId()).orElseThrow();
        if (!confirmStart || !"START_PENDING".equals(trip.getStatus())) {
            throw ApiException.unprocessable(ErrorCode.TRIP_START_CONFIRMATION_NOT_ALLOWED, "Trip cannot be started");
        }
        String scope = "POST:/api/v1/bookings/" + bookingId + "/start-confirmation";
        return idempotencyService.checkExisting(scope, idempotencyKey, scope)
                .map(existing -> Map.<String, Object>of("trip_id", existing.responseReferenceId(), "trip_state", "IN_PROGRESS"))
                .orElseGet(() -> {
                    HandoverChecklistEntity checklist = new HandoverChecklistEntity();
                    checklist.setTripId(trip.getId());
                    checklist.setConfirmedDriverMatch(true);
                    checklist.setFuelNote(fuelNote);
                    checklist.setVisibleConcernNote(damageNote);
                    checklist.setConfirmedByCustomerId(customerId);
                    checklist.setConfirmedAt(OffsetDateTime.now());
                    handoverChecklistRepository.save(checklist);
                    trip.setCustomerStartConfirmedAt(OffsetDateTime.now());
                    trip.setStartedAt(OffsetDateTime.now());
                    trip.setStatus("IN_PROGRESS");
                    tripRepository.save(trip);
                    booking.setStatus("IN_PROGRESS");
                    bookingRepository.save(booking);
                    appendTripEvent(trip.getId(), "trip.started", Map.of());
                    outboxService.publish("trip", trip.getId(), "TripStartedEvent", Map.of("trip_id", trip.getId(), "booking_id", booking.getId()));
                    idempotencyService.store(scope, idempotencyKey, scope, "TRIP", trip.getId(), "SUCCEEDED");
                    return Map.of(
                            "updated_booking_detail", Map.of("booking_id", booking.getId(), "state", booking.getStatus()),
                            "trip_id", trip.getId(),
                            "trip_state", "IN_PROGRESS"
                    );
                });
    }

    @Transactional
    public Map<String, Object> ingestLocation(UUID tripId, List<LocationPoint> points) {
        TripEntity trip = requireDriverTrip(tripId);
        TrackingSessionEntity session = trackingSessionRepository.findByTripId(tripId).orElseThrow();
        int accepted = 0;
        OffsetDateTime lastAt = null;
        for (LocationPoint point : points.stream().limit(10).toList()) {
            if (point.capturedAt().isBefore(OffsetDateTime.now().minusSeconds(rydvrseProperties.getTracking().getStaleThresholdSeconds() * 5))) {
                continue;
            }
            LocationPingEntity ping = new LocationPingEntity();
            ping.setTrackingSessionId(session.getId());
            ping.setDriverProfileId(trip.getDriverProfileId());
            ping.setPingAt(point.capturedAt());
            ping.setLatitude(point.latitude());
            ping.setLongitude(point.longitude());
            ping.setAccuracyMeters(point.accuracyMeters());
            ping.setSpeedKph(point.speedKmph());
            ping.setHeadingDegrees(point.headingDegrees());
            ping.setSourceType("DRIVER_APP");
            locationPingRepository.save(ping);
            accepted++;
            lastAt = point.capturedAt();
        }
        session.setLastPingAt(lastAt == null ? session.getLastPingAt() : lastAt);
        trackingSessionRepository.save(session);
        return Map.of("accepted_point_count", accepted, "last_processed_timestamp", lastAt);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> tripDetail(UUID tripId) {
        TripEntity trip = requireTripVisibleToActor(tripId);
        return tripSummary(trip);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> tracking(UUID tripId) {
        requireTripVisibleToActor(tripId);
        TrackingSessionEntity session = trackingSessionRepository.findByTripId(tripId).orElseThrow();
        LocationPingEntity ping = locationPingRepository.findTop20ByTrackingSessionIdOrderByPingAtDesc(session.getId()).stream().findFirst().orElse(null);
        return Map.of(
                "trip_id", tripId,
                "trip_state", tripRepository.findById(tripId).map(TripEntity::getStatus).orElse("UNKNOWN"),
                "driver_location", ping == null ? null : Map.of(
                        "latitude", ping.getLatitude(),
                        "longitude", ping.getLongitude(),
                        "captured_at", ping.getPingAt()
                ),
                "route_polyline", "",
                "eta_minutes", 10,
                "updated_at", session.getLastPingAt()
        );
    }

    @Transactional(readOnly = true)
    public SseEmitter trackingStream(UUID tripId) {
        requireTripVisibleToActor(tripId);
        SseEmitter emitter = new SseEmitter(30_000L);
        try {
            emitter.send(SseEmitter.event()
                    .name("trip.state_changed")
                    .data(Map.of("trip_id", tripId, "state", tripRepository.findById(tripId).map(TripEntity::getStatus).orElse("UNKNOWN"))));
            emitter.complete();
        } catch (Exception exception) {
            emitter.completeWithError(exception);
        }
        return emitter;
    }

    @Transactional
    public Map<String, Object> shareLink(UUID tripId, Integer expiresInMinutes) {
        UUID customerId = customerProfileService.requireCurrentProfile().getId();
        TripEntity trip = tripRepository.findById(tripId)
                .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "Trip not found"));
        if (!customerId.equals(trip.getCustomerProfileId())) {
            throw ApiException.forbidden(ErrorCode.FORBIDDEN, "Trip not visible");
        }
        String rawToken = "shr_" + UUID.randomUUID();
        TripShareLinkEntity shareLink = new TripShareLinkEntity();
        shareLink.setTripId(tripId);
        shareLink.setCustomerProfileId(customerId);
        shareLink.setShareTokenHash(HashingUtils.sha256(rawToken));
        shareLink.setStatus("ACTIVE");
        shareLink.setExpiresAt(OffsetDateTime.now().plusMinutes(expiresInMinutes == null ? 60 : expiresInMinutes));
        tripShareLinkRepository.save(shareLink);
        return Map.of(
                "share_url", "https://share.rydvrse.local/trips/" + tripId + "?token=" + rawToken,
                "expires_at", shareLink.getExpiresAt()
        );
    }

    @Transactional
    public Map<String, Object> sos(UUID tripId, String message, String idempotencyKey) {
        Map<String, Object> ticket = supportTicketService.createCustomerTicket(
                new SupportTicketService.CreateTicketCommand("SAFETY", "SOS", "CRITICAL", message == null ? "Emergency assistance requested" : message, null, tripId),
                idempotencyKey
        );
        return Map.of("incident_id", ticket.get("ticket_id"), "escalation_state", "OPEN");
    }

    @Transactional
    public Map<String, Object> complete(UUID tripId, String completionNote, String idempotencyKey) {
        TripEntity trip = requireDriverTrip(tripId);
        BookingEntity booking = bookingRepository.findById(trip.getBookingId()).orElseThrow();
        if ("COMPLETED".equals(trip.getStatus())) {
            throw ApiException.unprocessable(ErrorCode.TRIP_ALREADY_COMPLETED, "Trip already completed");
        }
        String scope = "POST:/api/v1/drivers/trips/" + tripId + "/complete";
        return idempotencyService.checkExisting(scope, idempotencyKey, scope)
                .map(existing -> Map.<String, Object>of("trip_id", existing.responseReferenceId(), "state", "COMPLETED"))
                .orElseGet(() -> {
                    OffsetDateTime completedAt = OffsetDateTime.now();
                    trip.setCompletedAt(completedAt);
                    trip.setStatus("COMPLETED");
                    trip.setFinalDurationMinutes(Math.max(1, (int) Duration.between(trip.getStartedAt() == null ? trip.getCreatedAt() : trip.getStartedAt(), completedAt).toMinutes()));
                    trip.setFinalTotalPaise(booking.getCurrentTotalPaise());
                    tripRepository.save(trip);
                    booking.setStatus("COMPLETED_PAYMENT_PENDING");
                    bookingRepository.save(booking);
                    appendTripEvent(tripId, "trip.completed", Map.of("note", completionNote == null ? "" : completionNote));

                    DriverEarningLedgerEntity ledger = new DriverEarningLedgerEntity();
                    ledger.setDriverProfileId(trip.getDriverProfileId());
                    ledger.setTripId(trip.getId());
                    ledger.setAssignmentId(trip.getAssignmentId());
                    ledger.setLedgerStatus("LOCKED");
                    ledger.setGrossPayoutPaise(Math.round(booking.getCurrentTotalPaise() * 0.70));
                    ledger.setAdjustmentPaise(0);
                    ledger.setNetPayoutPaise(Math.round(booking.getCurrentTotalPaise() * 0.70));
                    ledger.setLockedAt(OffsetDateTime.now());
                    ledger.setSnapshotPayload(jsonNodeUtils.toJsonNode(Map.of("share", 0.70, "fare_total_paise", booking.getCurrentTotalPaise())));
                    driverEarningLedgerRepository.save(ledger);

                    InvoiceEntity invoice = new InvoiceEntity();
                    invoice.setBookingId(booking.getId());
                    invoice.setTripId(trip.getId());
                    invoice.setInvoiceNumber("INV-" + System.currentTimeMillis());
                    invoice.setCurrencyCode("INR");
                    long subtotal = Math.round(booking.getCurrentTotalPaise() / 1.18);
                    invoice.setSubtotalPaise(subtotal);
                    invoice.setTaxPaise(booking.getCurrentTotalPaise() - subtotal);
                    invoice.setTotalPaise(booking.getCurrentTotalPaise());
                    invoice.setInvoiceStatus("ISSUED");
                    invoice.setIssuedAt(OffsetDateTime.now());
                    invoice.setSnapshotPayload(jsonNodeUtils.toJsonNode(Map.of("trip_id", tripId, "booking_id", booking.getId())));
                    invoiceRepository.save(invoice);

                    driverAvailabilityStatusRepository.findById(trip.getDriverProfileId()).ifPresent(status -> {
                        status.setCurrentStatus("OFFLINE");
                        status.setCurrentAssignmentId(null);
                        status.setCurrentBookingId(null);
                        status.setCurrentTripId(null);
                        driverAvailabilityStatusRepository.save(status);
                    });

                    outboxService.publish("trip", trip.getId(), "TripCompletedEvent", Map.of("trip_id", trip.getId(), "booking_id", booking.getId()));
                    idempotencyService.store(scope, idempotencyKey, scope, "TRIP", trip.getId(), "SUCCEEDED");
                    return Map.of(
                            "completed_trip_summary", tripSummary(trip),
                            "final_customer_fare_snapshot", Map.of("total_paise", booking.getCurrentTotalPaise()),
                            "final_driver_payout_snapshot", Map.of("total_payout_paise", ledger.getNetPayoutPaise())
                    );
                });
    }

    @Transactional
    public Map<String, Object> createPaymentOrder(UUID bookingId, String paymentMethod, String idempotencyKey) {
        BookingEntity booking = bookingRepository.findById(bookingId).orElseThrow();
        if (!List.of("COMPLETED_PAYMENT_PENDING", "COMPLETED").contains(booking.getStatus())) {
            throw ApiException.conflict(ErrorCode.STATE_CONFLICT, "Booking not ready for payment");
        }
        String scope = "POST:/api/v1/bookings/" + bookingId + "/payment-orders";
        return idempotencyService.checkExisting(scope, idempotencyKey, scope)
                .map(existing -> payment(existing.responseReferenceId()))
                .orElseGet(() -> {
                    PaymentOrderEntity order = paymentOrderRepository.findByBookingId(bookingId).orElseGet(PaymentOrderEntity::new);
                    order.setBookingId(bookingId);
                    order.setTripId(booking.getCurrentTripId());
                    order.setInvoiceId(invoiceRepository.findByBookingId(bookingId).map(InvoiceEntity::getId).orElse(null));
                    order.setProviderName("SANDBOX");
                    order.setProviderOrderId("pay_" + System.currentTimeMillis());
                    order.setCurrencyCode("INR");
                    order.setAmountPaise(booking.getCurrentTotalPaise());
                    order.setPaymentMethodType(paymentMethod);
                    order.setExpiresAt(OffsetDateTime.now().plusMinutes(15));
                    order.setMetadata(jsonNodeUtils.toJsonNode(Map.of("sandbox_auto_capture", rydvrseProperties.getPayments().isSandboxAutoCapture())));
                    order.setStatus(rydvrseProperties.getPayments().isSandboxAutoCapture() ? "CAPTURED" : "CREATED");
                    paymentOrderRepository.save(order);
                    if ("CAPTURED".equals(order.getStatus())) {
                        booking.setPaymentState("CAPTURED");
                        booking.setStatus("COMPLETED");
                        bookingRepository.save(booking);
                    }
                    idempotencyService.store(scope, idempotencyKey, scope, "PAYMENT_ORDER", order.getId(), "SUCCEEDED");
                    return payment(order.getId());
                });
    }

    @Transactional(readOnly = true)
    public Map<String, Object> payment(UUID paymentId) {
        PaymentOrderEntity order = paymentOrderRepository.findById(paymentId).orElseThrow();
        return Map.of(
                "payment_id", order.getId(),
                "booking_id", order.getBookingId(),
                "state", order.getStatus(),
                "provider", order.getProviderName(),
                "provider_order_id", order.getProviderOrderId(),
                "amount_paise", order.getAmountPaise(),
                "payment_method", order.getPaymentMethodType(),
                "expires_at", order.getExpiresAt(),
                "provider_payment_reference", order.getProviderOrderId(),
                "failure_reason", ""
        );
    }

    @Transactional(readOnly = true)
    public Map<String, Object> invoice(UUID bookingId) {
        InvoiceEntity invoice = invoiceRepository.findByBookingId(bookingId).orElseThrow();
        return Map.of(
                "invoice_id", invoice.getId(),
                "fare_breakdown", invoice.getSnapshotPayload(),
                "tax_breakdown", Map.of("tax_paise", invoice.getTaxPaise()),
                "payment_summary", paymentOrderRepository.findByBookingId(bookingId).map(order -> Map.of("state", order.getStatus(), "payment_id", order.getId())).orElse(Map.of("state", "PENDING")),
                "refund_summary", Map.of("state", "NONE")
        );
    }

    private TripEntity requireDriverTrip(UUID tripId) {
        UUID driverId = driverProfileService.requireCurrentProfile().getId();
        TripEntity trip = tripRepository.findById(tripId)
                .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "Trip not found"));
        if (!driverId.equals(trip.getDriverProfileId())) {
            throw ApiException.forbidden(ErrorCode.FORBIDDEN, "Trip not visible");
        }
        return trip;
    }

    private TripEntity requireTripVisibleToActor(UUID tripId) {
        TripEntity trip = tripRepository.findById(tripId)
                .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "Trip not found"));
        return switch (currentActorService.requireCurrentActor().actorType()) {
            case DRIVER -> {
                if (!driverProfileService.requireCurrentProfile().getId().equals(trip.getDriverProfileId())) {
                    throw ApiException.forbidden(ErrorCode.FORBIDDEN, "Trip not visible");
                }
                yield trip;
            }
            case CUSTOMER -> {
                if (!customerProfileService.requireCurrentProfile().getId().equals(trip.getCustomerProfileId())) {
                    throw ApiException.forbidden(ErrorCode.FORBIDDEN, "Trip not visible");
                }
                yield trip;
            }
            case ADMIN -> trip;
        };
    }

    private void appendTripEvent(UUID tripId, String eventType, Map<String, Object> payload) {
        TripEventEntity event = new TripEventEntity();
        event.setTripId(tripId);
        event.setEventType(eventType);
        event.setEventAt(OffsetDateTime.now());
        event.setActorType(currentActorService.getCurrentActor().map(actor -> actor.actorType().name()).orElse("SYSTEM"));
        event.setActorUserId(currentActorService.getCurrentActor().map(actor -> actor.userId()).orElse(null));
        event.setEventPayload(jsonNodeUtils.toJsonNode(payload));
        tripEventRepository.save(event);
    }

    private Map<String, Object> toAssignmentSummary(AssignmentEntity assignment) {
        BookingEntity booking = bookingRepository.findById(assignment.getBookingId()).orElse(null);
        long payoutPreview = booking == null ? 0 : Math.round(booking.getCurrentTotalPaise() * 0.70);
        return Map.of(
                "assignment_id", assignment.getId(),
                "state", assignment.getStatus(),
                "eta_minutes", assignment.getDriverEtaSeconds() == null ? null : assignment.getDriverEtaSeconds() / 60,
                "assigned_at", assignment.getAssignedAt(),
                "reassignment_count", Math.max(0, assignment.getAssignmentSequenceNo() - 1),
                "earning_preview", Map.of("total_payout_paise", payoutPreview)
        );
    }

    private Map<String, Object> tripSummary(TripEntity trip) {
        BookingEntity booking = bookingRepository.findById(trip.getBookingId()).orElse(null);
        List<Map<String, Object>> timeline = tripEventRepository.findByTripIdOrderByEventAtAsc(trip.getId()).stream()
                .map(event -> Map.of(
                        "event_type", event.getEventType(),
                        "event_at", event.getEventAt(),
                        "payload", event.getEventPayload()
                )).toList();
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("trip_id", trip.getId());
        detail.put("state", trip.getStatus());
        detail.put("booking_id", trip.getBookingId());
        detail.put("driver_id", trip.getDriverProfileId());
        detail.put("customer_id", trip.getCustomerProfileId());
        detail.put("booking_summary", booking == null ? null : Map.of(
                "service_type", booking.getServiceType(),
                "scheduled_pickup_at", booking.getScheduledPickupAt(),
                "pickup_address", booking.getPickupAddressText(),
                "drop_address", booking.getDropAddressText()
        ));
        detail.put("timeline", timeline);
        detail.put("final_total_paise", trip.getFinalTotalPaise());
        detail.put("safety_actions_summary", Map.of(
                "handover_confirmed", handoverChecklistRepository.findByTripId(trip.getId()).isPresent(),
                "share_links_count", tripShareLinkRepository.findByTripIdOrderByCreatedAtDesc(trip.getId()).size()
        ));
        return detail;
    }

    public record LocationPoint(
            BigDecimal latitude,
            BigDecimal longitude,
            OffsetDateTime capturedAt,
            BigDecimal accuracyMeters,
            BigDecimal speedKmph,
            BigDecimal headingDegrees
    ) {
    }
}
