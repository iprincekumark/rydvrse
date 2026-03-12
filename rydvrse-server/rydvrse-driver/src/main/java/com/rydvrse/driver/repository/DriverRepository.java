package com.rydvrse.driver.repository;

import com.rydvrse.driver.domain.Driver;
import com.rydvrse.shared.enums.DriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DriverRepository extends JpaRepository<Driver, UUID> {
    Optional<Driver> findByAuthUserId(UUID authUserId);
    Optional<Driver> findByPhoneNumber(String phoneNumber);
    boolean existsByPhoneNumber(String phoneNumber);
    List<Driver> findByStatusAndIsAvailableTrueAndIsOnTripFalse(DriverStatus status);

    @Query("SELECT d FROM Driver d WHERE d.status = 'ACTIVE' AND d.isAvailable = true " +
           "AND d.isOnTrip = false AND d.operatingCity = :city")
    List<Driver> findAvailableDriversInCity(String city);
}
