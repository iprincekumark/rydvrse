package com.rydvrse.trip.infrastructure;

import com.rydvrse.trip.domain.LocationPingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LocationPingRepository extends JpaRepository<LocationPingEntity, UUID> {

    List<LocationPingEntity> findTop20ByTrackingSessionIdOrderByPingAtDesc(UUID trackingSessionId);
}
