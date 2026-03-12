package com.rydvrse.location.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Location service — real-time GPS tracking using Redis.
 *
 * Stores driver locations in Redis using GEO data structures
 * for ultra-fast spatial queries (finding nearby drivers).
 *
 * Also stores latest coordinates as hash for individual lookups.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocationService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String DRIVER_LOCATION_KEY = "rydvrse:driver:locations";
    private static final String DRIVER_LOCATION_HASH = "rydvrse:driver:location:";
    private static final long LOCATION_TTL_MINUTES = 30;

    /**
     * Update driver's GPS location.
     * Called frequently by driver app (every 5-10 seconds during active trips).
     */
    public void updateDriverLocation(UUID driverId, double latitude, double longitude) {
        // Store in Redis GEO set for spatial queries
        redisTemplate.opsForGeo().add(DRIVER_LOCATION_KEY,
                new org.springframework.data.geo.Point(longitude, latitude),
                driverId.toString());

        // Store latest coordinates as Redis hash
        String key = DRIVER_LOCATION_HASH + driverId;
        Map<String, String> locationData = Map.of(
                "lat", String.valueOf(latitude),
                "lng", String.valueOf(longitude),
                "timestamp", String.valueOf(System.currentTimeMillis())
        );
        redisTemplate.opsForHash().putAll(key, locationData);
        redisTemplate.expire(key, LOCATION_TTL_MINUTES, TimeUnit.MINUTES);

        log.debug("Updated location for driver {}: [{}, {}]", driverId, latitude, longitude);
    }

    /**
     * Get driver's current location.
     */
    public Map<String, Double> getDriverLocation(UUID driverId) {
        String key = DRIVER_LOCATION_HASH + driverId;
        Map<Object, Object> data = redisTemplate.opsForHash().entries(key);
        if (data.isEmpty()) return null;

        return Map.of(
                "lat", Double.parseDouble((String) data.get("lat")),
                "lng", Double.parseDouble((String) data.get("lng"))
        );
    }

    /**
     * Find nearby drivers within a given radius (in km).
     * Uses Redis GEORADIUS for O(N+log(M)) performance.
     */
    public List<String> findNearbyDrivers(double latitude, double longitude, double radiusKm) {
        var results = redisTemplate.opsForGeo().radius(
                DRIVER_LOCATION_KEY,
                new org.springframework.data.geo.Circle(
                        new org.springframework.data.geo.Point(longitude, latitude),
                        new org.springframework.data.geo.Distance(radiusKm,
                                org.springframework.data.redis.connection.RedisGeoCommands.DistanceUnit.KILOMETERS)
                )
        );

        if (results == null) return Collections.emptyList();
        return results.getContent().stream()
                .map(r -> r.getContent().getName())
                .toList();
    }

    /**
     * Remove driver location (when going offline).
     */
    public void removeDriverLocation(UUID driverId) {
        redisTemplate.opsForGeo().remove(DRIVER_LOCATION_KEY, driverId.toString());
        redisTemplate.delete(DRIVER_LOCATION_HASH + driverId);
    }
}
