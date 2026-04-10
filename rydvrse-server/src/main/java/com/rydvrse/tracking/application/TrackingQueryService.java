package com.rydvrse.tracking.application;

import com.rydvrse.common.error.ApiException;
import com.rydvrse.common.error.ErrorCode;
import com.rydvrse.common.security.ActorType;
import com.rydvrse.common.security.CurrentActorService;
import com.rydvrse.customer.application.CustomerProfileService;
import com.rydvrse.dispatch.domain.AssignmentEntity;
import com.rydvrse.dispatch.infrastructure.AssignmentRepository;
import com.rydvrse.driver.application.DriverProfileService;
import com.rydvrse.trip.domain.LocationPingEntity;
import com.rydvrse.trip.domain.TrackingSessionEntity;
import com.rydvrse.trip.domain.TripEntity;
import com.rydvrse.trip.domain.TripShareLinkEntity;
import com.rydvrse.trip.infrastructure.LocationPingRepository;
import com.rydvrse.trip.infrastructure.TrackingSessionRepository;
import com.rydvrse.trip.infrastructure.TripRepository;
import com.rydvrse.trip.infrastructure.TripShareLinkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TrackingQueryService {

    private final TripRepository tripRepository;
    private final TrackingSessionRepository trackingSessionRepository;
    private final LocationPingRepository locationPingRepository;
    private final TripShareLinkRepository tripShareLinkRepository;
    private final AssignmentRepository assignmentRepository;
    private final CustomerProfileService customerProfileService;
    private final DriverProfileService driverProfileService;
    private final CurrentActorService currentActorService;
    private final EtaEstimationService etaEstimationService;

    public TrackingQueryService(
            TripRepository tripRepository,
            TrackingSessionRepository trackingSessionRepository,
            LocationPingRepository locationPingRepository,
            TripShareLinkRepository tripShareLinkRepository,
            AssignmentRepository assignmentRepository,
            CustomerProfileService customerProfileService,
            DriverProfileService driverProfileService,
            CurrentActorService currentActorService,
            EtaEstimationService etaEstimationService
    ) {
        this.tripRepository = tripRepository;
        this.trackingSessionRepository = trackingSessionRepository;
        this.locationPingRepository = locationPingRepository;
        this.tripShareLinkRepository = tripShareLinkRepository;
        this.assignmentRepository = assignmentRepository;
        this.customerProfileService = customerProfileService;
        this.driverProfileService = driverProfileService;
        this.currentActorService = currentActorService;
        this.etaEstimationService = etaEstimationService;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> trackingSnapshot(UUID tripId) {
        TripEntity trip = requireVisibleTrip(tripId);
        TrackingSessionEntity session = trackingSessionRepository.findByTripId(tripId)
                .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "Tracking session not found"));
        LocationPingEntity ping = locationPingRepository.findTop20ByTrackingSessionIdOrderByPingAtDesc(session.getId()).stream().findFirst().orElse(null);
        AssignmentEntity assignment = assignmentRepository.findById(trip.getAssignmentId()).orElse(null);
        TripShareLinkEntity shareLink = tripShareLinkRepository.findFirstByTripIdAndStatusOrderByCreatedAtDesc(tripId, "ACTIVE").orElse(null);

        return Map.of(
                "trip_id", tripId,
                "trip_state", trip.getStatus(),
                "driver_location", ping == null ? null : Map.of(
                        "latitude", ping.getLatitude(),
                        "longitude", ping.getLongitude(),
                        "captured_at", ping.getPingAt()
                ),
                "route_polyline", "",
                "eta_minutes", etaEstimationService.estimateMinutes(trip, assignment, ping),
                "updated_at", session.getLastPingAt() == null ? session.getStartedAt() : session.getLastPingAt(),
                "share_url", shareLink == null ? null : "https://share.rydvrse.local/trips/" + tripId
        );
    }

    private TripEntity requireVisibleTrip(UUID tripId) {
        TripEntity trip = tripRepository.findById(tripId)
                .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "Trip not found"));
        return switch (currentActorService.requireCurrentActor().actorType()) {
            case ADMIN -> trip;
            case CUSTOMER -> {
                if (!customerProfileService.requireCurrentProfile().getId().equals(trip.getCustomerProfileId())) {
                    throw ApiException.forbidden(ErrorCode.FORBIDDEN, "Trip not visible");
                }
                yield trip;
            }
            case DRIVER -> {
                if (!driverProfileService.requireCurrentProfile().getId().equals(trip.getDriverProfileId())) {
                    throw ApiException.forbidden(ErrorCode.FORBIDDEN, "Trip not visible");
                }
                yield trip;
            }
        };
    }
}
