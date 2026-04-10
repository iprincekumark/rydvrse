package com.rydvrse.tracking.application;

import com.rydvrse.common.config.RydvrseProperties;
import com.rydvrse.common.error.ApiException;
import com.rydvrse.common.error.ErrorCode;
import com.rydvrse.driver.application.DriverProfileService;
import com.rydvrse.trip.domain.LocationPingEntity;
import com.rydvrse.trip.domain.TripEntity;
import com.rydvrse.trip.infrastructure.LocationPingRepository;
import com.rydvrse.trip.infrastructure.TripRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class LocationIngestionService {

    private final TripRepository tripRepository;
    private final LocationPingRepository locationPingRepository;
    private final DriverProfileService driverProfileService;
    private final TrackingSessionService trackingSessionService;
    private final TrackingQueryService trackingQueryService;
    private final TrackingProjectionService trackingProjectionService;
    private final TrackingStreamService trackingStreamService;
    private final RydvrseProperties rydvrseProperties;

    public LocationIngestionService(
            TripRepository tripRepository,
            LocationPingRepository locationPingRepository,
            DriverProfileService driverProfileService,
            TrackingSessionService trackingSessionService,
            TrackingQueryService trackingQueryService,
            TrackingProjectionService trackingProjectionService,
            TrackingStreamService trackingStreamService,
            RydvrseProperties rydvrseProperties
    ) {
        this.tripRepository = tripRepository;
        this.locationPingRepository = locationPingRepository;
        this.driverProfileService = driverProfileService;
        this.trackingSessionService = trackingSessionService;
        this.trackingQueryService = trackingQueryService;
        this.trackingProjectionService = trackingProjectionService;
        this.trackingStreamService = trackingStreamService;
        this.rydvrseProperties = rydvrseProperties;
    }

    @Transactional
    public Map<String, Object> ingest(UUID tripId, List<LocationPoint> points) {
        TripEntity trip = requireDriverTrip(tripId);
        UUID sessionId = trackingSessionService.openForTrip(tripId).getId();
        int accepted = 0;
        OffsetDateTime lastAt = null;
        for (LocationPoint point : points.stream().limit(10).toList()) {
            if (point.capturedAt().isBefore(OffsetDateTime.now().minusSeconds(rydvrseProperties.getTracking().getStaleThresholdSeconds() * 5))) {
                continue;
            }
            LocationPingEntity ping = new LocationPingEntity();
            ping.setTrackingSessionId(sessionId);
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

        if (lastAt != null) {
            trackingSessionService.updateLastPing(tripId, lastAt);
            Map<String, Object> snapshot = trackingQueryService.trackingSnapshot(tripId);
            trackingProjectionService.updateTripSnapshot(tripId, snapshot);
            trackingStreamService.publish(tripId, "trip.location_updated", snapshot);
        }
        return Map.of("accepted_point_count", accepted, "last_processed_timestamp", lastAt);
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
