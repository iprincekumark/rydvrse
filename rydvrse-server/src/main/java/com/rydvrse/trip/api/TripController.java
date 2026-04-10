package com.rydvrse.trip.api;

import com.rydvrse.common.api.ApiResponse;
import com.rydvrse.finance.application.FinanceOperationsService;
import com.rydvrse.trip.application.TripExecutionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
public class TripController {

    private final TripExecutionService tripExecutionService;
    private final FinanceOperationsService financeOperationsService;

    public TripController(TripExecutionService tripExecutionService, FinanceOperationsService financeOperationsService) {
        this.tripExecutionService = tripExecutionService;
        this.financeOperationsService = financeOperationsService;
    }

    @PostMapping("/api/v1/drivers/trips/{tripId}/arrived")
    public ApiResponse<Map<String, Object>> arrived(@PathVariable UUID tripId) {
        return ApiResponse.of(tripExecutionService.markArrived(tripId), null);
    }

    @PostMapping("/api/v1/bookings/{bookingId}/start-confirmation")
    public ApiResponse<Map<String, Object>> startConfirmation(
            @PathVariable UUID bookingId,
            @Valid @RequestBody StartConfirmationRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    ) {
        return ApiResponse.of(tripExecutionService.confirmStart(bookingId, request.confirmStart(), request.fuelLevelNote(), request.damageNote(), idempotencyKey), null);
    }

    @GetMapping("/api/v1/trips/{tripId}")
    public ApiResponse<Map<String, Object>> trip(@PathVariable UUID tripId) {
        return ApiResponse.of(tripExecutionService.tripDetail(tripId), null);
    }

    @GetMapping("/api/v1/drivers/trips/{tripId}")
    public ApiResponse<Map<String, Object>> driverTrip(@PathVariable UUID tripId) {
        return ApiResponse.of(tripExecutionService.tripDetail(tripId), null);
    }

    @PostMapping("/api/v1/trips/{tripId}/sos")
    public ApiResponse<Map<String, Object>> sos(
            @PathVariable UUID tripId,
            @RequestBody Map<String, String> payload,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    ) {
        return ApiResponse.of(tripExecutionService.sos(tripId, payload.get("message"), idempotencyKey), null);
    }

    @PostMapping("/api/v1/drivers/trips/{tripId}/complete")
    public ApiResponse<Map<String, Object>> complete(
            @PathVariable UUID tripId,
            @RequestBody Map<String, String> payload,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    ) {
        return ApiResponse.of(tripExecutionService.complete(tripId, payload.get("completion_note"), idempotencyKey), null);
    }

    @PostMapping("/api/v1/bookings/{bookingId}/payment-orders")
    public ApiResponse<Map<String, Object>> createPayment(
            @PathVariable UUID bookingId,
            @RequestBody Map<String, String> payload,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    ) {
        return ApiResponse.of(financeOperationsService.createPaymentOrder(bookingId, payload.get("payment_method"), idempotencyKey), null);
    }

    @GetMapping("/api/v1/payments/{paymentId}")
    public ApiResponse<Map<String, Object>> payment(@PathVariable UUID paymentId) {
        return ApiResponse.of(financeOperationsService.payment(paymentId), null);
    }

    @GetMapping("/api/v1/bookings/{bookingId}/invoice")
    public ApiResponse<Map<String, Object>> invoice(@PathVariable UUID bookingId) {
        return ApiResponse.of(financeOperationsService.invoice(bookingId), null);
    }

    public record StartConfirmationRequest(@NotNull Boolean confirmStart, String fuelLevelNote, String damageNote) {
    }
}
