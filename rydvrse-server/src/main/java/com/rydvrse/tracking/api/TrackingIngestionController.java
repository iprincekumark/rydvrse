package com.rydvrse.tracking.api;

import com.rydvrse.common.api.ApiResponse;
import com.rydvrse.tracking.application.LocationIngestionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class TrackingIngestionController {

    private final LocationIngestionService locationIngestionService;

    public TrackingIngestionController(LocationIngestionService locationIngestionService) {
        this.locationIngestionService = locationIngestionService;
    }

    @PostMapping("/api/v1/drivers/trips/{tripId}/location-pings")
    public ApiResponse<Map<String, Object>> locationPings(@PathVariable UUID tripId, @Valid @RequestBody LocationPingRequest request) {
        return ApiResponse.of(locationIngestionService.ingest(
                tripId,
                request.points().stream().map(point -> new LocationIngestionService.LocationPoint(
                        point.latitude(),
                        point.longitude(),
                        point.capturedAt(),
                        point.accuracyMeters(),
                        point.speedKmph(),
                        point.headingDegrees()
                )).toList()
        ), null);
    }

    public record LocationPingRequest(@NotNull List<LocationPointRequest> points) {
    }

    public record LocationPointRequest(
            @NotNull BigDecimal latitude,
            @NotNull BigDecimal longitude,
            @NotNull OffsetDateTime capturedAt,
            BigDecimal accuracyMeters,
            BigDecimal speedKmph,
            BigDecimal headingDegrees
    ) {
    }
}
