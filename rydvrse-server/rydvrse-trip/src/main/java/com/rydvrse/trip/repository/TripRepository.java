package com.rydvrse.trip.repository;

import com.rydvrse.trip.entity.Trip;
import com.rydvrse.shared.enums.TripStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TripRepository extends JpaRepository<Trip, UUID> {
    Page<Trip> findByCustomerId(UUID customerId, Pageable pageable);
    Page<Trip> findByDriverId(UUID driverId, Pageable pageable);
    List<Trip> findByCustomerIdAndStatus(UUID customerId, TripStatus status);
    List<Trip> findByDriverIdAndStatus(UUID driverId, TripStatus status);
    long countByStatus(TripStatus status);
}
