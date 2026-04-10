package com.rydvrse.booking.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.rydvrse.booking.domain.BookingEntity;
import com.rydvrse.booking.infrastructure.BookingRepository;
import com.rydvrse.common.error.ApiException;
import com.rydvrse.common.error.ErrorCode;
import com.rydvrse.common.idempotency.IdempotencyService;
import com.rydvrse.common.outbox.OutboxService;
import com.rydvrse.customer.application.CustomerProfileService;
import com.rydvrse.pricing.domain.CancellationPolicyEntity;
import com.rydvrse.pricing.infrastructure.CancellationPolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class BookingCancellationService {

    private final BookingRepository bookingRepository;
    private final CancellationPolicyRepository cancellationPolicyRepository;
    private final CustomerProfileService customerProfileService;
    private final BookingStateService bookingStateService;
    private final IdempotencyService idempotencyService;
    private final OutboxService outboxService;

    public BookingCancellationService(
            BookingRepository bookingRepository,
            CancellationPolicyRepository cancellationPolicyRepository,
            CustomerProfileService customerProfileService,
            BookingStateService bookingStateService,
            IdempotencyService idempotencyService,
            OutboxService outboxService
    ) {
        this.bookingRepository = bookingRepository;
        this.cancellationPolicyRepository = cancellationPolicyRepository;
        this.customerProfileService = customerProfileService;
        this.bookingStateService = bookingStateService;
        this.idempotencyService = idempotencyService;
        this.outboxService = outboxService;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> preview(UUID bookingId) {
        BookingEntity booking = requireMine(bookingId);
        if (!List.of("PENDING_ASSIGNMENT", "ASSIGNED", "ARRIVED").contains(booking.getStatus())) {
            return Map.of(
                    "allowed", false,
                    "fee_paise", 0,
                    "driver_compensation_paise", 0,
                    "refund_estimate_paise", 0,
                    "reason_summary", "Booking cannot be cancelled in current state"
            );
        }
        CancellationPolicyEntity policy = cancellationPolicyRepository.findById(booking.getCancellationPolicyId())
                .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "Cancellation policy not found"));
        long freeUntilMinutesBeforePickup = policy.getPolicyPayload().path("free_until_minutes_before_pickup").asLong(60);
        boolean free = Duration.between(OffsetDateTime.now(), booking.getScheduledPickupAt()).toMinutes() >= freeUntilMinutesBeforePickup;
        long fee = free ? 0 : policy.getPolicyPayload().path("late_cancel_fee_paise").asLong(0);
        long driverComp = free ? 0 : policy.getPolicyPayload().path("driver_compensation_paise").asLong(0);
        return Map.of(
                "allowed", true,
                "fee_paise", fee,
                "driver_compensation_paise", driverComp,
                "refund_estimate_paise", Math.max(0, booking.getCurrentTotalPaise() - fee),
                "reason_summary", free ? "Cancellation is free" : "Late cancellation fees apply"
        );
    }

    @Transactional
    public Map<String, Object> cancel(UUID bookingId, CancelBookingCommand command, String idempotencyKey) {
        BookingEntity booking = requireMine(bookingId);
        String scope = "POST:/api/v1/bookings/" + bookingId + "/cancel";
        return idempotencyService.checkExisting(scope, idempotencyKey, idempotencyService.hashPayload(command))
                .map(existing -> {
                    Map<String, Object> result = new java.util.LinkedHashMap<>();
                    result.put("booking_id", existing.responseReferenceId());
                    result.put("state", "CANCELLED");
                    return result;
                })
                .orElseGet(() -> {
                    Map<String, Object> preview = preview(bookingId);
                    if (!(Boolean) preview.get("allowed")) {
                        throw ApiException.unprocessable(ErrorCode.BOOKING_CANCELLATION_NOT_ALLOWED, "Booking cannot be cancelled");
                    }
                    bookingStateService.transition(booking, "CANCELLED", command.reasonCode(), command.reasonNote());
                    booking.setActive(false);
                    bookingRepository.save(booking);
                    outboxService.publish("booking", booking.getId(), "BookingCancelledEvent", Map.of("booking_id", booking.getId()));
                    idempotencyService.store(scope, idempotencyKey, command, "BOOKING", booking.getId(), "SUCCEEDED");
                    Map<String, Object> result = new java.util.LinkedHashMap<>();
                    result.put("booking_detail", Map.of(
                            "booking_id", booking.getId(),
                            "state", "CANCELLED"
                    ));
                    result.put("cancellation_outcome", preview);
                    result.put("refund_summary", Map.of(
                            "estimated_refund_paise", preview.get("refund_estimate_paise")
                    ));
                    return result;
                });
    }

    private BookingEntity requireMine(UUID bookingId) {
        UUID customerProfileId = customerProfileService.requireCurrentProfile().getId();
        return bookingRepository.findByIdAndCustomerProfileId(bookingId, customerProfileId)
                .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "Booking not found"));
    }

    public record CancelBookingCommand(String reasonCode, String reasonNote) {
    }
}
