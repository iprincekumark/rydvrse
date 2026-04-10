package com.rydvrse.booking.api;

import com.rydvrse.booking.application.BookingCancellationService;
import com.rydvrse.booking.application.BookingCreationService;
import com.rydvrse.booking.application.BookingModificationService;
import com.rydvrse.booking.application.BookingQueryService;
import com.rydvrse.booking.application.BookingRatingService;
import com.rydvrse.common.api.ApiMeta;
import com.rydvrse.common.api.ApiResponse;
import com.rydvrse.common.util.RequestIdHolder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
public class CustomerBookingController {

    private final BookingCreationService bookingCreationService;
    private final BookingQueryService bookingQueryService;
    private final BookingCancellationService bookingCancellationService;
    private final BookingModificationService bookingModificationService;
    private final BookingRatingService bookingRatingService;

    public CustomerBookingController(
            BookingCreationService bookingCreationService,
            BookingQueryService bookingQueryService,
            BookingCancellationService bookingCancellationService,
            BookingModificationService bookingModificationService,
            BookingRatingService bookingRatingService
    ) {
        this.bookingCreationService = bookingCreationService;
        this.bookingQueryService = bookingQueryService;
        this.bookingCancellationService = bookingCancellationService;
        this.bookingModificationService = bookingModificationService;
        this.bookingRatingService = bookingRatingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Map<String, Object>> create(
            @Valid @RequestBody CreateBookingRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    ) {
        UUID bookingId = bookingCreationService.createBooking(new BookingCreationService.CreateBookingCommand(
                request.quoteId(),
                request.contactName(),
                request.contactMobileNumber(),
                request.customerNotes(),
                request.passengerName(),
                request.passengerMobileNumber(),
                request.handoverNote()
        ), idempotencyKey);
        return ApiResponse.of(bookingQueryService.getMine(bookingId), null);
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> list() {
        List<Map<String, Object>> bookings = bookingQueryService.listMine();
        return new ApiResponse<>(bookings, ApiMeta.now(RequestIdHolder.get(), Map.of("limit", bookings.size(), "next_cursor", null)));
    }

    @GetMapping("/{bookingId}")
    public ApiResponse<Map<String, Object>> get(@PathVariable UUID bookingId) {
        return ApiResponse.of(bookingQueryService.getMine(bookingId), null);
    }

    @GetMapping("/{bookingId}/cancellation-preview")
    public ApiResponse<Map<String, Object>> cancellationPreview(@PathVariable UUID bookingId) {
        return ApiResponse.of(bookingCancellationService.preview(bookingId), null);
    }

    @PostMapping("/{bookingId}/modification-preview")
    public ApiResponse<Map<String, Object>> modificationPreview(@PathVariable UUID bookingId, @Valid @RequestBody ModifyBookingRequest request) {
        return ApiResponse.of(bookingModificationService.preview(
                bookingId,
                new BookingModificationService.ModificationCommand(
                        request.scheduledPickupAt(),
                        request.expectedDurationMinutes(),
                        request.pickupLabel(),
                        request.pickupAddressLine1(),
                        request.pickupAddressLine2(),
                        request.pickupLandmark(),
                        request.pickupLatitude(),
                        request.pickupLongitude(),
                        request.dropLabel(),
                        request.dropAddressLine1(),
                        request.dropAddressLine2(),
                        request.dropLandmark(),
                        request.dropLatitude(),
                        request.dropLongitude(),
                        request.customerNotes()
                )
        ), null);
    }

    @PostMapping("/{bookingId}/modify")
    public ApiResponse<Map<String, Object>> modify(
            @PathVariable UUID bookingId,
            @Valid @RequestBody ModifyBookingRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    ) {
        return ApiResponse.of(bookingModificationService.apply(
                bookingId,
                new BookingModificationService.ModificationCommand(
                        request.scheduledPickupAt(),
                        request.expectedDurationMinutes(),
                        request.pickupLabel(),
                        request.pickupAddressLine1(),
                        request.pickupAddressLine2(),
                        request.pickupLandmark(),
                        request.pickupLatitude(),
                        request.pickupLongitude(),
                        request.dropLabel(),
                        request.dropAddressLine1(),
                        request.dropAddressLine2(),
                        request.dropLandmark(),
                        request.dropLatitude(),
                        request.dropLongitude(),
                        request.customerNotes()
                ),
                request.previewQuoteId(),
                idempotencyKey
        ), null);
    }

    @PostMapping("/{bookingId}/cancel")
    public ApiResponse<Map<String, Object>> cancel(
            @PathVariable UUID bookingId,
            @Valid @RequestBody CancelBookingRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    ) {
        return ApiResponse.of(bookingCancellationService.cancel(
                bookingId,
                new BookingCancellationService.CancelBookingCommand(request.reasonCode(), request.reasonNote()),
                idempotencyKey
        ), null);
    }

    @PostMapping("/{bookingId}/ratings")
    public ApiResponse<Map<String, Object>> rate(
            @PathVariable UUID bookingId,
            @Valid @RequestBody RatingRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    ) {
        return ApiResponse.of(bookingRatingService.submit(bookingId, request.rating(), request.tags(), request.comment(), idempotencyKey), null);
    }

    public record CreateBookingRequest(
            @NotNull UUID quoteId,
            String contactName,
            String contactMobileNumber,
            String customerNotes,
            String passengerName,
            String passengerMobileNumber,
            String handoverNote
    ) {
    }

    public record CancelBookingRequest(@NotBlank String reasonCode, String reasonNote) {
    }

    public record ModifyBookingRequest(
            UUID previewQuoteId,
            java.time.OffsetDateTime scheduledPickupAt,
            Integer expectedDurationMinutes,
            String pickupLabel,
            String pickupAddressLine1,
            String pickupAddressLine2,
            String pickupLandmark,
            java.math.BigDecimal pickupLatitude,
            java.math.BigDecimal pickupLongitude,
            String dropLabel,
            String dropAddressLine1,
            String dropAddressLine2,
            String dropLandmark,
            java.math.BigDecimal dropLatitude,
            java.math.BigDecimal dropLongitude,
            String customerNotes
    ) {
    }

    public record RatingRequest(@NotNull Integer rating, List<String> tags, String comment) {
    }
}
