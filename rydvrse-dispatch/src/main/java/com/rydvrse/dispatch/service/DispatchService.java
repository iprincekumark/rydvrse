package com.rydvrse.dispatch.service;

import com.rydvrse.driver.domain.Driver;
import com.rydvrse.driver.repository.DriverRepository;
import com.rydvrse.driver.service.DriverService;
import com.rydvrse.shared.domain.GeoLocation;
import com.rydvrse.shared.event.EventPublisher;
import com.rydvrse.shared.exception.BusinessRuleException;
import com.rydvrse.trip.event.TripRequestedEvent;
import com.rydvrse.trip.service.TripService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Dispatch service — matches drivers with trip requests.
 *
 * Matching algorithm:
 * 1. Find all available drivers in the pickup city
 * 2. Filter by distance (within search radius)
 * 3. Score by: distance (40%) + rating (30%) + acceptance rate (30%)
 * 4. Select highest-scoring driver
 * 5. Assign driver to trip
 *
 * Listens to: TripRequestedEvent
 * Calls: TripService.assignDriver(), DriverService.markOnTrip()
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DispatchService {

    private final DriverRepository driverRepository;
    private final DriverService driverService;
    private final TripService tripService;
    private final EventPublisher eventPublisher;

    @Value("${rydvrse.dispatch.search-radius-km:10.0}")
    private double searchRadiusKm;

    @Value("${rydvrse.dispatch.timeout-seconds:30}")
    private int timeoutSeconds;

    /**
     * Event listener: when a trip is requested, find and assign a driver.
     */
    @EventListener
    @Async
    @Transactional
    public void onTripRequested(TripRequestedEvent event) {
        log.info("Dispatch: searching for driver for trip {} in city {}",
                event.getAggregateId(), event.getPickupCity());

        GeoLocation pickupLocation = GeoLocation.builder()
                .latitude(event.getPickupLat())
                .longitude(event.getPickupLng())
                .build();

        // Find available drivers in the city
        List<Driver> availableDrivers = driverRepository
                .findAvailableDriversInCity(event.getPickupCity());

        if (availableDrivers.isEmpty()) {
            log.warn("No available drivers found in city: {}", event.getPickupCity());
            // In production: retry with expanding radius, then cancel
            return;
        }

        // Score and rank drivers
        Driver bestDriver = availableDrivers.stream()
                .filter(d -> d.getCurrentLocation() != null)
                .filter(d -> {
                    double distance = pickupLocation.distanceTo(d.getCurrentLocation());
                    return distance <= searchRadiusKm;
                })
                .max(Comparator.comparingDouble(d -> computeScore(d, pickupLocation)))
                .orElse(null);

        if (bestDriver == null) {
            log.warn("No nearby drivers within {}km radius for trip {}",
                    searchRadiusKm, event.getAggregateId());
            return;
        }

        // Assign driver to trip
        tripService.assignDriver(event.getAggregateId(), bestDriver.getId());
        driverService.markOnTrip(bestDriver.getId(), true);

        log.info("Driver {} assigned to trip {} (rating: {}, distance: {}km)",
                bestDriver.getId(), event.getAggregateId(),
                bestDriver.getAverageRating(),
                pickupLocation.distanceTo(bestDriver.getCurrentLocation()));
    }

    /**
     * Compute driver matching score.
     * Higher score = better match.
     */
    private double computeScore(Driver driver, GeoLocation pickup) {
        double distance = pickup.distanceTo(driver.getCurrentLocation());
        double distanceScore = Math.max(0, (searchRadiusKm - distance) / searchRadiusKm); // 0-1
        double ratingScore = driver.getAverageRating() / 5.0; // 0-1
        double acceptanceScore = driver.getAcceptanceRate() / 100.0; // 0-1

        return (distanceScore * 0.4) + (ratingScore * 0.3) + (acceptanceScore * 0.3);
    }
}
