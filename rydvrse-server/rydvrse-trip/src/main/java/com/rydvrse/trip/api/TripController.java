package com.rydvrse.trip.api;

import com.rydvrse.shared.dto.ApiResponse;
import com.rydvrse.shared.dto.PagedResponse;
import com.rydvrse.trip.domain.Trip;
import com.rydvrse.trip.dto.CreateTripRequest;
import com.rydvrse.trip.service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @PostMapping
    public ResponseEntity<ApiResponse<Trip>> createTrip(@Valid @RequestBody CreateTripRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Trip created", tripService.createTrip(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Trip>> getTrip(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(tripService.getTrip(id)));
    }

    @PostMapping("/{id}/assign-driver")
    public ResponseEntity<ApiResponse<Trip>> assignDriver(
            @PathVariable UUID id, @RequestBody Map<String, UUID> body) {
        return ResponseEntity.ok(ApiResponse.success(tripService.assignDriver(id, body.get("driverId"))));
    }

    @PostMapping("/{id}/driver-arrived")
    public ResponseEntity<ApiResponse<Trip>> driverArrived(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(tripService.driverArrived(id)));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<ApiResponse<Trip>> startTrip(
            @PathVariable UUID id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.success(tripService.startTrip(id, body.get("otp"))));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<Trip>> completeTrip(
            @PathVariable UUID id, @RequestBody Map<String, Object> body) {
        Double distance = body.get("actualDistanceKm") != null ? ((Number) body.get("actualDistanceKm")).doubleValue() : null;
        Integer duration = body.get("actualDurationMin") != null ? ((Number) body.get("actualDurationMin")).intValue() : null;
        return ResponseEntity.ok(ApiResponse.success(tripService.completeTrip(id, distance, duration)));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Trip>> cancelTrip(
            @PathVariable UUID id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.success(
                tripService.cancelTrip(id, body.get("reason"), body.get("cancelledBy"))));
    }

    @PostMapping("/{id}/rate")
    public ResponseEntity<ApiResponse<Void>> rateTrip(
            @PathVariable UUID id, @RequestBody Map<String, Object> body) {
        tripService.rateDriver(id, ((Number) body.get("rating")).doubleValue(), (String) body.get("feedback"));
        return ResponseEntity.ok(ApiResponse.success("Rating submitted", null));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<PagedResponse<Trip>>> getCustomerTrips(
            @PathVariable UUID customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Trip> trips = tripService.getCustomerTrips(customerId, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.<Trip>builder()
                .content(trips.getContent()).page(page).size(size)
                .totalElements(trips.getTotalElements()).totalPages(trips.getTotalPages())
                .last(trips.isLast()).build()));
    }
}
