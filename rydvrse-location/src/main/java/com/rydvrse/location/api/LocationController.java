package com.rydvrse.location.api;

import com.rydvrse.location.service.LocationService;
import com.rydvrse.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;
    private final SimpMessagingTemplate messagingTemplate;

    /** Driver sends GPS update */
    @PostMapping("/driver/{driverId}")
    public ResponseEntity<ApiResponse<Void>> updateLocation(
            @PathVariable UUID driverId, @RequestBody Map<String, Double> body) {
        locationService.updateDriverLocation(driverId, body.get("latitude"), body.get("longitude"));
        return ResponseEntity.ok(ApiResponse.success("Location updated", null));
    }

    /** Get driver's current location */
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<ApiResponse<Map<String, Double>>> getLocation(@PathVariable UUID driverId) {
        return ResponseEntity.ok(ApiResponse.success(locationService.getDriverLocation(driverId)));
    }

    /** Push live location to trip subscribers via WebSocket */
    @PostMapping("/driver/{driverId}/trip/{tripId}")
    public ResponseEntity<ApiResponse<Void>> updateTripLocation(
            @PathVariable UUID driverId, @PathVariable UUID tripId,
            @RequestBody Map<String, Double> body) {
        locationService.updateDriverLocation(driverId, body.get("latitude"), body.get("longitude"));
        // Push to WebSocket subscribers
        messagingTemplate.convertAndSend("/topic/trip/" + tripId + "/location", body);
        return ResponseEntity.ok(ApiResponse.success("Location streamed", null));
    }

    /** Find nearby drivers */
    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<String>>> findNearby(
            @RequestParam double lat, @RequestParam double lng,
            @RequestParam(defaultValue = "5.0") double radiusKm) {
        return ResponseEntity.ok(ApiResponse.success(
                locationService.findNearbyDrivers(lat, lng, radiusKm)));
    }
}
