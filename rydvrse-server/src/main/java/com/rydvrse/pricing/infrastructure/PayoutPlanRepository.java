package com.rydvrse.pricing.infrastructure;

import com.rydvrse.pricing.domain.PayoutPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface PayoutPlanRepository extends JpaRepository<PayoutPlanEntity, UUID> {

    List<PayoutPlanEntity> findByCityIdAndStatusAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(UUID cityId, String status, OffsetDateTime effectiveFrom);

    List<PayoutPlanEntity> findAllByOrderByCreatedAtDesc();
}
