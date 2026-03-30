package com.rydvrse.driver.repository;

import com.rydvrse.driver.entity.DriverAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DriverAvailabilityRepository extends JpaRepository<DriverAvailability, UUID> {
    Optional<DriverAvailability> findByDriverId(UUID driverId);
}
