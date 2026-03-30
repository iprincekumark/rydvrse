package com.rydvrse.trip.repository;

import com.rydvrse.trip.entity.FareRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FareRuleRepository extends JpaRepository<FareRule, UUID> {
    @Query("SELECT fr FROM FareRule fr WHERE fr.vehicleType = :vehicleType AND fr.effectiveFrom <= CURRENT_DATE AND (fr.effectiveTo IS NULL OR fr.effectiveTo >= CURRENT_DATE)")
    Optional<FareRule> findActiveByVehicleType(String vehicleType);
}
