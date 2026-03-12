package com.rydvrse.trip.repository;

import com.rydvrse.shared.enums.TripStatus;
import com.rydvrse.trip.domain.Trip;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TripRepository extends JpaRepository<Trip, UUID> {
    Optional<Trip> findByTripNumber(String tripNumber);
    Page<Trip> findByCustomerIdOrderByCreatedAtDesc(UUID customerId, Pageable pageable);
    Page<Trip> findByDriverIdOrderByCreatedAtDesc(UUID driverId, Pageable pageable);
    List<Trip> findByCustomerIdAndStatus(UUID customerId, TripStatus status);
    List<Trip> findByDriverIdAndStatus(UUID driverId, TripStatus status);
    long countByCustomerId(UUID customerId);
    long countByDriverId(UUID driverId);
}
