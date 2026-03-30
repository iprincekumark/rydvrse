package com.rydvrse.trip.controller;

import com.rydvrse.shared.dto.ApiResponse;
import com.rydvrse.shared.enums.RatedBy;
import com.rydvrse.shared.enums.TripCancelledBy;
import com.rydvrse.shared.security.UserPrincipal;
import com.rydvrse.trip.entity.Trip;
import com.rydvrse.trip.entity.TripRating;
import com.rydvrse.trip.service.TripService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
@Tag(name = "Trip", description = "Trip booking, lifecycle, and ratings")
public class TripController {

    private final TripService tripService;

    @PostMapping("/estimate")
    @Operation(summary = "Get fare estimate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> estimate(@RequestBody Map<String, Double> body) {
        BigDecimal fare = tripService.estimateFare(body.get("pickupLat"), body.get("pickupLng"),
                body.get("dropLat"), body.get("dropLng"));
        return ResponseEntity.ok(ApiResponse.success(Map.of("estimatedFare", fare)));
    }

    @PostMapping("/book")
    @Operation(summary = "Book a trip")
    public ResponseEntity<ApiResponse<Trip>> book(@AuthenticationPrincipal UserPrincipal principal,
                                                   @RequestBody Map<String, Object> body) {
        Trip trip = tripService.bookTrip(
                principal.getUserId(),
                (String) body.get("pickupAddress"), ((Number) body.get("pickupLat")).doubleValue(),
                ((Number) body.get("pickupLng")).doubleValue(), (String) body.get("dropAddress"),
                ((Number) body.get("dropLat")).doubleValue(), ((Number) body.get("dropLng")).doubleValue(),
                body.containsKey("scheduledAt") ? Instant.parse((String) body.get("scheduledAt")) : null);
        return ResponseEntity.ok(ApiResponse.success(trip));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get trip details")
    public ResponseEntity<ApiResponse<Trip>> getTrip(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(tripService.getTrip(id)));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel trip")
    public ResponseEntity<ApiResponse<Trip>> cancel(@PathVariable UUID id,
                                                     @AuthenticationPrincipal UserPrincipal principal,
                                                     @RequestBody Map<String, String> body) {
        TripCancelledBy cancelledBy = "DRIVER".equals(principal.getRole()) ?
                TripCancelledBy.DRIVER : TripCancelledBy.CUSTOMER;
        return ResponseEntity.ok(ApiResponse.success(
                tripService.cancelTrip(id, principal.getUserId(), cancelledBy, body.get("reason"))));
    }

    @PutMapping("/{id}/start")
    @Operation(summary = "Start the trip (Driver)")
    public ResponseEntity<ApiResponse<Trip>> start(@PathVariable UUID id,
                                                    @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(tripService.startTrip(id, principal.getUserId())));
    }

    @PutMapping("/{id}/complete")
    @Operation(summary = "Complete the trip (Driver)")
    public ResponseEntity<ApiResponse<Trip>> complete(@PathVariable UUID id,
                                                       @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(tripService.completeTrip(id, principal.getUserId())));
    }

    @PostMapping("/{id}/rate")
    @Operation(summary = "Rate the trip")
    public ResponseEntity<ApiResponse<TripRating>> rate(@PathVariable UUID id,
                                                         @AuthenticationPrincipal UserPrincipal principal,
                                                         @RequestBody Map<String, Object> body) {
        RatedBy ratedBy = "DRIVER".equals(principal.getRole()) ? RatedBy.DRIVER : RatedBy.CUSTOMER;
        return ResponseEntity.ok(ApiResponse.success(tripService.rateTrip(
                id, principal.getUserId(), ratedBy,
                ((Number) body.get("rating")).intValue(), (String) body.get("review"))));
    }
}
