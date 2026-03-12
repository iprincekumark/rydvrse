package com.rydvrse.vehicle.repository;

import com.rydvrse.vehicle.domain.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {
    Optional<Vehicle> findByRegistrationNumber(String registrationNumber);
    List<Vehicle> findByOwnerId(UUID ownerId);
    boolean existsByRegistrationNumber(String registrationNumber);
}
