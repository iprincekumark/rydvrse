package com.rydvrse.pricing.infrastructure;

import com.rydvrse.pricing.domain.CancellationPolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface CancellationPolicyRepository extends JpaRepository<CancellationPolicyEntity, UUID> {

    List<CancellationPolicyEntity> findByCityIdAndStatusAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(UUID cityId, String status, OffsetDateTime effectiveFrom);
}
