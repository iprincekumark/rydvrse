package com.rydvrse.trip.infrastructure;

import com.rydvrse.trip.domain.TrackingSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TrackingSessionRepository extends JpaRepository<TrackingSessionEntity, UUID> {

    Optional<TrackingSessionEntity> findByTripId(UUID tripId);

    List<TrackingSessionEntity> findByStatusAndLastPingAtBefore(String status, OffsetDateTime lastPingAt);
}
