package com.rydvrse.trip.infrastructure;

import com.rydvrse.trip.domain.TripEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TripRepository extends JpaRepository<TripEntity, UUID> {

    Optional<TripEntity> findByBookingId(UUID bookingId);

    java.util.List<TripEntity> findByDriverProfileIdOrderByCreatedAtDesc(UUID driverProfileId);
}
