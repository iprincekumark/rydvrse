package com.rydvrse.trip.service;

import com.rydvrse.shared.enums.RatedBy;
import com.rydvrse.shared.enums.TripCancelledBy;
import com.rydvrse.shared.enums.TripStatus;
import com.rydvrse.shared.event.EventPublisher;
import com.rydvrse.shared.exception.BusinessRuleException;
import com.rydvrse.shared.exception.ResourceNotFoundException;
import com.rydvrse.trip.entity.*;
import com.rydvrse.trip.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final TripRatingRepository ratingRepository;
    private final FareRuleRepository fareRuleRepository;
    private final TripStateMachine stateMachine;
    private final EventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public BigDecimal estimateFare(double pickupLat, double pickupLng, double dropLat, double dropLng) {
        double distance = haversineDistance(pickupLat, pickupLng, dropLat, dropLng);
        int duration = (int) (distance / 0.5); // ~30 km/h avg
        FareRule rule = fareRuleRepository.findActiveByVehicleType("SEDAN")
                .orElseGet(this::defaultFareRule);

        BigDecimal fare = rule.getBaseFare()
                .add(rule.getPerKmRate().multiply(BigDecimal.valueOf(distance)))
                .add(rule.getPerMinuteRate().multiply(BigDecimal.valueOf(duration)));

        if (isNightTime()) {
            fare = fare.multiply(BigDecimal.ONE.add(rule.getNightSurchargePercent().divide(BigDecimal.valueOf(100))));
        }

        return fare.max(rule.getMinimumFare());
    }

    @Transactional
    public Trip bookTrip(UUID customerId, String pickupAddress, double pickupLat, double pickupLng,
                         String dropAddress, double dropLat, double dropLng, Instant scheduledAt) {
        BigDecimal fare = estimateFare(pickupLat, pickupLng, dropLat, dropLng);
        double distance = haversineDistance(pickupLat, pickupLng, dropLat, dropLng);

        Trip trip = Trip.builder()
                .customerId(customerId)
                .pickupAddress(pickupAddress)
                .pickupLat(pickupLat)
                .pickupLng(pickupLng)
                .dropAddress(dropAddress)
                .dropLat(dropLat)
                .dropLng(dropLng)
                .estimatedFare(fare)
                .estimatedDistance(BigDecimal.valueOf(distance))
                .estimatedDuration((int) (distance / 0.5))
                .scheduledAt(scheduledAt)
                .status(TripStatus.SEARCHING_DRIVER)
                .isNightTrip(isNightTime())
                .build();

        trip = tripRepository.save(trip);
        log.info("Trip booked: {} by customer {}", trip.getId(), customerId);
        return trip;
    }

    @Transactional
    public Trip cancelTrip(UUID tripId, UUID userId, TripCancelledBy cancelledBy, String reason) {
        Trip trip = findById(tripId);
        stateMachine.validateTransition(trip.getStatus(), TripStatus.CANCELLED);

        trip.setStatus(TripStatus.CANCELLED);
        trip.setCancelledAt(Instant.now());
        trip.setCancelledBy(cancelledBy);
        trip.setCancellationReason(reason);
        return tripRepository.save(trip);
    }

    @Transactional
    public Trip startTrip(UUID tripId, UUID driverId) {
        Trip trip = findById(tripId);
        validateDriverOwnership(trip, driverId);
        stateMachine.validateTransition(trip.getStatus(), TripStatus.IN_PROGRESS);

        trip.setStatus(TripStatus.IN_PROGRESS);
        trip.setStartedAt(Instant.now());
        return tripRepository.save(trip);
    }

    @Transactional
    public Trip completeTrip(UUID tripId, UUID driverId) {
        Trip trip = findById(tripId);
        validateDriverOwnership(trip, driverId);
        stateMachine.validateTransition(trip.getStatus(), TripStatus.COMPLETED);

        trip.setStatus(TripStatus.COMPLETED);
        trip.setCompletedAt(Instant.now());
        trip.setActualFare(trip.getEstimatedFare()); // Simplified for MVP
        trip.setActualDistance(trip.getEstimatedDistance());
        return tripRepository.save(trip);
    }

    @Transactional
    public TripRating rateTrip(UUID tripId, UUID raterId, RatedBy ratedBy, int rating, String review) {
        Trip trip = findById(tripId);
        if (trip.getStatus() != TripStatus.COMPLETED) {
            throw new BusinessRuleException("TRIP_NOT_COMPLETED", "Can only rate completed trips");
        }

        TripRating tripRating = TripRating.builder()
                .tripId(tripId)
                .raterId(raterId)
                .ratedBy(ratedBy)
                .rating(rating)
                .review(review)
                .build();
        return ratingRepository.save(tripRating);
    }

    @Transactional(readOnly = true)
    public Trip getTrip(UUID tripId) {
        return findById(tripId);
    }

    @Transactional(readOnly = true)
    public Page<Trip> getCustomerTrips(UUID customerId, Pageable pageable) {
        return tripRepository.findByCustomerId(customerId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Trip> getDriverTrips(UUID driverId, Pageable pageable) {
        return tripRepository.findByDriverId(driverId, pageable);
    }

    public Trip findById(UUID id) {
        return tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip", id.toString()));
    }

    private void validateDriverOwnership(Trip trip, UUID driverId) {
        if (!driverId.equals(trip.getDriverId())) {
            throw new BusinessRuleException("NOT_ASSIGNED_DRIVER", "You are not the assigned driver for this trip");
        }
    }

    private boolean isNightTime() {
        LocalTime now = LocalTime.now();
        return now.isAfter(LocalTime.of(23, 0)) || now.isBefore(LocalTime.of(5, 0));
    }

    private double haversineDistance(double lat1, double lng1, double lat2, double lng2) {
        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private FareRule defaultFareRule() {
        return FareRule.builder()
                .baseFare(BigDecimal.valueOf(50))
                .perKmRate(BigDecimal.valueOf(12))
                .perMinuteRate(BigDecimal.valueOf(2))
                .minimumFare(BigDecimal.valueOf(80))
                .nightSurchargePercent(BigDecimal.valueOf(25))
                .cancellationFee(BigDecimal.valueOf(50))
                .build();
    }
}
