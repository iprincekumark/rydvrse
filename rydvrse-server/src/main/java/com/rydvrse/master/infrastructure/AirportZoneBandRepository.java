package com.rydvrse.master.infrastructure;

import com.rydvrse.master.domain.AirportZoneBandEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AirportZoneBandRepository extends JpaRepository<AirportZoneBandEntity, UUID> {

    Optional<AirportZoneBandEntity> findByCityIdAndServiceZoneId(UUID cityId, UUID serviceZoneId);
}
