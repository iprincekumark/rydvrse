package com.rydvrse.driver.repository;

import com.rydvrse.driver.entity.DriverLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DriverLocationRepository extends JpaRepository<DriverLocation, UUID> {

    Optional<DriverLocation> findByDriverId(UUID driverId);

    /**
     * Find nearby driver IDs using PostGIS ST_DWithin spatial query.
     * @param lng longitude
     * @param lat latitude
     * @param radiusMeters search radius in meters
     */
    @Query(value = """
            SELECT dl.driver_id FROM driver_location dl
            WHERE ST_DWithin(
                dl.point::geography,
                ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography,
                :radiusMeters
            )
            ORDER BY dl.point <-> ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)
            """, nativeQuery = true)
    List<UUID> findNearbyDriverIds(double lng, double lat, double radiusMeters);
}
