package com.rydvrse.pricing.infrastructure;

import com.rydvrse.pricing.domain.PricingPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface PricingPlanRepository extends JpaRepository<PricingPlanEntity, UUID> {

    List<PricingPlanEntity> findByCityIdAndStatusAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(UUID cityId, String status, OffsetDateTime effectiveFrom);

    List<PricingPlanEntity> findAllByOrderByCreatedAtDesc();
}
