package com.rydvrse.pricing.infrastructure;

import com.rydvrse.pricing.domain.PayoutRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PayoutRuleRepository extends JpaRepository<PayoutRuleEntity, UUID> {

    List<PayoutRuleEntity> findByPayoutPlanIdOrderByServiceTypeAscRuleTypeAsc(UUID payoutPlanId);

    List<PayoutRuleEntity> findByPayoutPlanIdAndServiceTypeAndActiveTrue(UUID payoutPlanId, String serviceType);
}
