package com.rydvrse.pricing.infrastructure;

import com.rydvrse.pricing.domain.RefundPolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface RefundPolicyRepository extends JpaRepository<RefundPolicyEntity, UUID> {

    List<RefundPolicyEntity> findByCityIdAndStatusAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(UUID cityId, String status, OffsetDateTime effectiveFrom);
}
