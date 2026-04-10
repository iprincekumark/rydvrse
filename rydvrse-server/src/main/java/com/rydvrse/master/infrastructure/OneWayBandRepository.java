package com.rydvrse.master.infrastructure;

import com.rydvrse.master.domain.OneWayBandEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OneWayBandRepository extends JpaRepository<OneWayBandEntity, UUID> {

    Optional<OneWayBandEntity> findByCityIdAndSourceZoneIdAndDestinationZoneId(UUID cityId, UUID sourceZoneId, UUID destinationZoneId);
}
