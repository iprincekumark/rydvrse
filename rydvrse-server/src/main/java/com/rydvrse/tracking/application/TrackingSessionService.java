package com.rydvrse.tracking.application;

import com.rydvrse.common.config.RydvrseProperties;
import com.rydvrse.trip.domain.TrackingSessionEntity;
import com.rydvrse.trip.infrastructure.TrackingSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TrackingSessionService {

    private final TrackingSessionRepository trackingSessionRepository;
    private final RydvrseProperties rydvrseProperties;

    public TrackingSessionService(TrackingSessionRepository trackingSessionRepository, RydvrseProperties rydvrseProperties) {
        this.trackingSessionRepository = trackingSessionRepository;
        this.rydvrseProperties = rydvrseProperties;
    }

    @Transactional
    public TrackingSessionEntity openForTrip(UUID tripId) {
        return trackingSessionRepository.findByTripId(tripId).orElseGet(() -> {
            TrackingSessionEntity session = new TrackingSessionEntity();
            session.setTripId(tripId);
            session.setStatus("ACTIVE");
            session.setStartedAt(OffsetDateTime.now());
            return trackingSessionRepository.save(session);
        });
    }

    @Transactional
    public TrackingSessionEntity updateLastPing(UUID tripId, OffsetDateTime lastPingAt) {
        TrackingSessionEntity session = openForTrip(tripId);
        session.setStatus("ACTIVE");
        session.setLastPingAt(lastPingAt);
        return trackingSessionRepository.save(session);
    }

    @Transactional
    public int markStaleSessions() {
        OffsetDateTime cutoff = OffsetDateTime.now().minusSeconds(rydvrseProperties.getTracking().getStaleThresholdSeconds());
        List<TrackingSessionEntity> staleSessions = trackingSessionRepository.findByStatusAndLastPingAtBefore("ACTIVE", cutoff);
        staleSessions.forEach(session -> session.setStatus("DEGRADED"));
        trackingSessionRepository.saveAll(staleSessions);
        return staleSessions.size();
    }
}
