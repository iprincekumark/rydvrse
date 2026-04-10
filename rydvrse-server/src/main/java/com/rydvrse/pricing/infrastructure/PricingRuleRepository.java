package com.rydvrse.pricing.infrastructure;

import com.rydvrse.pricing.domain.PricingRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PricingRuleRepository extends JpaRepository<PricingRuleEntity, UUID> {

    List<PricingRuleEntity> findByPricingPlanIdAndServiceTypeAndActiveTrue(UUID pricingPlanId, String serviceType);

    List<PricingRuleEntity> findByPricingPlanIdOrderByServiceTypeAscRuleTypeAsc(UUID pricingPlanId);
}
