package com.rydvrse.tracking.api;

import com.rydvrse.common.api.ApiResponse;
import com.rydvrse.tracking.application.TrackingQueryService;
import com.rydvrse.tracking.application.TrackingStreamService;
import com.rydvrse.tracking.application.TripShareLinkService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.UUID;

@RestController
public class TrackingController {

    private final TrackingQueryService trackingQueryService;
    private final TrackingStreamService trackingStreamService;
    private final TripShareLinkService tripShareLinkService;

    public TrackingController(
            TrackingQueryService trackingQueryService,
            TrackingStreamService trackingStreamService,
            TripShareLinkService tripShareLinkService
    ) {
        this.trackingQueryService = trackingQueryService;
        this.trackingStreamService = trackingStreamService;
        this.tripShareLinkService = tripShareLinkService;
    }

    @GetMapping("/api/v1/trips/{tripId}/tracking")
    public ApiResponse<Map<String, Object>> tracking(@PathVariable UUID tripId) {
        return ApiResponse.of(trackingQueryService.trackingSnapshot(tripId), null);
    }

    @GetMapping("/api/v1/trips/{tripId}/tracking/stream")
    public SseEmitter trackingStream(@PathVariable UUID tripId) {
        return trackingStreamService.subscribe(tripId, () -> trackingQueryService.trackingSnapshot(tripId));
    }

    @PostMapping("/api/v1/trips/{tripId}/share-links")
    public ApiResponse<Map<String, Object>> shareLinks(@PathVariable UUID tripId, @RequestBody Map<String, Integer> payload) {
        return ApiResponse.of(tripShareLinkService.createShareLink(tripId, payload.get("expires_in_minutes")), null);
    }
}
