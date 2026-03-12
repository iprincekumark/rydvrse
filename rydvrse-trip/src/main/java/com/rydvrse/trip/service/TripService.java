package com.rydvrse.trip.service;

import com.rydvrse.shared.domain.GeoLocation;
import com.rydvrse.shared.enums.TripStatus;
import com.rydvrse.shared.event.EventPublisher;
import com.rydvrse.shared.exception.BusinessRuleException;
import com.rydvrse.shared.exception.ResourceNotFoundException;
import com.rydvrse.trip.domain.Trip;
import com.rydvrse.trip.dto.CreateTripRequest;
import com.rydvrse.trip.event.TripCompletedEvent;
import com.rydvrse.trip.event.TripRequestedEvent;
import com.rydvrse.trip.event.TripStatusChangedEvent;
import com.rydvrse.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Trip lifecycle service — the CORE business logic of RYDVRSE.
 *
 * Manages the full trip state machine:
 * REQUESTED → DRIVER_MATCHING → DRIVER_ASSIGNED → DRIVER_ARRIVING
 *           → TRIP_STARTED → TRIP_COMPLETED
 *
 * Every state transition emits domain events consumed by:
 * - Dispatch (for matching), Payment (for billing), Notification (for alerts),
 * - Location (for tracking), Safety (for monitoring), Analytics (for metrics)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final EventPublisher eventPublisher;

    private static final Set<TripStatus> CANCELLABLE_STATES = Set.of(
            TripStatus.REQUESTED, TripStatus.DRIVER_MATCHING,
            TripStatus.DRIVER_ASSIGNED, TripStatus.DRIVER_ARRIVING
    );

    /**
     * Step 1: Customer requests a trip.
     * Creates trip in REQUESTED state and publishes TripRequestedEvent.
     * Dispatch module listens and begins driver matching.
     */
    @Transactional
    public Trip createTrip(CreateTripRequest request) {
        GeoLocation pickup = GeoLocation.builder()
                .latitude(request.getPickupLat()).longitude(request.getPickupLng())
                .address(request.getPickupAddress()).city(request.getPickupCity())
                .build();
        GeoLocation drop = GeoLocation.builder()
                .latitude(request.getDropLat()).longitude(request.getDropLng())
                .address(request.getDropAddress()).city(request.getDropCity())
                .build();

        double distance = pickup.distanceTo(drop);

        Trip trip = Trip.builder()
                .customerId(request.getCustomerId())
                .vehicleId(request.getVehicleId())
                .pickupLocation(pickup)
                .dropLocation(drop)
                .estimatedDistanceKm(Math.round(distance * 100.0) / 100.0)
                .estimatedFare(request.getEstimatedFare())
                .surgeMultiplier(request.getSurgeMultiplier() != null ? request.getSurgeMultiplier() : 1.0)
                .status(TripStatus.REQUESTED)
                .startOtp(generateOtp())
                .build();

        Trip saved = tripRepository.save(trip);
        log.info("Trip created: {} for customer {}", saved.getTripNumber(), saved.getCustomerId());

        // Publish event → triggers Dispatch module to find a driver
        eventPublisher.publish(new TripRequestedEvent(
                saved.getId(), saved.getCustomerId(),
                pickup.getLatitude(), pickup.getLongitude(), pickup.getCity()));
        eventPublisher.publishAsync(new TripRequestedEvent(
                saved.getId(), saved.getCustomerId(),
                pickup.getLatitude(), pickup.getLongitude(), pickup.getCity()));

        return saved;
    }

    /**
     * Step 2: Dispatch assigns a driver to this trip.
     */
    @Transactional
    public Trip assignDriver(UUID tripId, UUID driverId) {
        Trip trip = getTrip(tripId);
        validateTransition(trip.getStatus(), TripStatus.DRIVER_ASSIGNED);

        trip.setDriverId(driverId);
        trip.setStatus(TripStatus.DRIVER_ASSIGNED);
        trip.setDriverAssignedAt(Instant.now());
        Trip saved = tripRepository.save(trip);

        publishStatusChange(saved, TripStatus.DRIVER_MATCHING.name(), TripStatus.DRIVER_ASSIGNED.name());
        log.info("Driver {} assigned to trip {}", driverId, trip.getTripNumber());
        return saved;
    }

    /**
     * Step 3: Driver arrives at pickup location.
     */
    @Transactional
    public Trip driverArrived(UUID tripId) {
        Trip trip = getTrip(tripId);
        validateTransition(trip.getStatus(), TripStatus.DRIVER_ARRIVING);

        trip.setStatus(TripStatus.DRIVER_ARRIVING);
        trip.setDriverArrivedAt(Instant.now());
        Trip saved = tripRepository.save(trip);

        publishStatusChange(saved, TripStatus.DRIVER_ASSIGNED.name(), TripStatus.DRIVER_ARRIVING.name());
        return saved;
    }

    /**
     * Step 4: Trip starts (OTP verified by driver).
     */
    @Transactional
    public Trip startTrip(UUID tripId, String otp) {
        Trip trip = getTrip(tripId);
        if (!trip.getStartOtp().equals(otp)) {
            throw new BusinessRuleException("INVALID_OTP", "Invalid trip start OTP");
        }

        trip.setStatus(TripStatus.TRIP_STARTED);
        trip.setTripStartedAt(Instant.now());
        Trip saved = tripRepository.save(trip);

        publishStatusChange(saved, TripStatus.DRIVER_ARRIVING.name(), TripStatus.TRIP_STARTED.name());
        log.info("Trip {} started", trip.getTripNumber());
        return saved;
    }

    /**
     * Step 5: Trip completes.
     * Publishes TripCompletedEvent → triggers Payment processing.
     */
    @Transactional
    public Trip completeTrip(UUID tripId, Double actualDistanceKm, Integer actualDurationMin) {
        Trip trip = getTrip(tripId);
        validateTransition(trip.getStatus(), TripStatus.TRIP_COMPLETED);

        trip.setStatus(TripStatus.TRIP_COMPLETED);
        trip.setTripCompletedAt(Instant.now());
        trip.setActualDistanceKm(actualDistanceKm);
        trip.setActualDurationMin(actualDurationMin);

        // Final fare calculation could be delegated to Pricing module
        if (trip.getFinalFare() == null) {
            trip.setFinalFare(trip.getEstimatedFare()); // Simplified: use estimate
        }

        Trip saved = tripRepository.save(trip);

        // Publish completion event → Payment, Analytics, Driver (update stats)
        var completedEvent = new TripCompletedEvent(
                saved.getId(), saved.getCustomerId(), saved.getDriverId(),
                saved.getFinalFare(), saved.getActualDistanceKm());
        eventPublisher.publish(completedEvent);
        eventPublisher.publishAsync(completedEvent);

        log.info("Trip {} completed. Fare: ₹{}", trip.getTripNumber(), trip.getFinalFare());
        return saved;
    }

    /**
     * Cancel trip (allowed only in pre-start states).
     */
    @Transactional
    public Trip cancelTrip(UUID tripId, String reason, String cancelledBy) {
        Trip trip = getTrip(tripId);
        if (!CANCELLABLE_STATES.contains(trip.getStatus())) {
            throw new BusinessRuleException("Cannot cancel trip in state: " + trip.getStatus());
        }

        String prevStatus = trip.getStatus().name();
        trip.setStatus(TripStatus.CANCELLED);
        trip.setCancelledAt(Instant.now());
        trip.setCancellationReason(reason);
        trip.setCancelledBy(cancelledBy);
        Trip saved = tripRepository.save(trip);

        publishStatusChange(saved, prevStatus, TripStatus.CANCELLED.name());
        log.info("Trip {} cancelled by {}: {}", trip.getTripNumber(), cancelledBy, reason);
        return saved;
    }

    /** Rate driver after trip */
    @Transactional
    public void rateDriver(UUID tripId, double rating, String feedback) {
        Trip trip = getTrip(tripId);
        if (trip.getStatus() != TripStatus.TRIP_COMPLETED) {
            throw new BusinessRuleException("Can only rate after trip completion");
        }
        trip.setDriverRating(rating);
        trip.setCustomerFeedback(feedback);
        tripRepository.save(trip);
    }

    @Transactional(readOnly = true)
    public Trip getTrip(UUID tripId) {
        return tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip", tripId.toString()));
    }

    @Transactional(readOnly = true)
    public Page<Trip> getCustomerTrips(UUID customerId, Pageable pageable) {
        return tripRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Trip> getDriverTrips(UUID driverId, Pageable pageable) {
        return tripRepository.findByDriverIdOrderByCreatedAtDesc(driverId, pageable);
    }

    // ---- Helpers ----

    private void validateTransition(TripStatus current, TripStatus target) {
        // Simplified validation — production would use a state machine
        if (current == TripStatus.TRIP_COMPLETED || current == TripStatus.CANCELLED) {
            throw new BusinessRuleException("Trip is already in terminal state: " + current);
        }
    }

    private void publishStatusChange(Trip trip, String prev, String next) {
        var event = new TripStatusChangedEvent(
                trip.getId(), prev, next, trip.getCustomerId(), trip.getDriverId());
        eventPublisher.publish(event);
        eventPublisher.publishAsync(event);
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        return String.format("%04d", random.nextInt(10000));
    }
}
