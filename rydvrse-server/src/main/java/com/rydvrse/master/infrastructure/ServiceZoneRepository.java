package com.rydvrse.master.infrastructure;

import com.rydvrse.master.domain.ServiceZoneEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceZoneRepository extends JpaRepository<ServiceZoneEntity, UUID> {

    List<ServiceZoneEntity> findByCityIdAndLaunchStatusOrderBySortOrderAsc(UUID cityId, String launchStatus);

    List<ServiceZoneEntity> findByCityIdOrderBySortOrderAsc(UUID cityId);

    List<ServiceZoneEntity> findAllByOrderByCreatedAtDesc();

    Optional<ServiceZoneEntity> findByIdAndCityId(UUID id, UUID cityId);

    @Query(value = """
            select *
            from master.service_zone zone
            where zone.city_id = :cityId
              and zone.launch_status = 'ACTIVE'
              and ST_Contains(zone.boundary_geom, ST_SetSRID(ST_MakePoint(cast(:longitude as double precision), cast(:latitude as double precision)), 4326))
            order by zone.sort_order asc
            limit 1
            """, nativeQuery = true)
    Optional<ServiceZoneEntity> findContainingZone(UUID cityId, BigDecimal latitude, BigDecimal longitude);
}
