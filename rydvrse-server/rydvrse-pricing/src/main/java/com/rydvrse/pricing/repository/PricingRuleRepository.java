package com.rydvrse.pricing.repository;

import com.rydvrse.pricing.domain.PricingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PricingRuleRepository extends JpaRepository<PricingRule, UUID> {
    Optional<PricingRule> findByCityAndVehicleTypeAndIsActiveTrue(String city, String vehicleType);
    Optional<PricingRule> findByCityAndIsActiveTrue(String city);
}
