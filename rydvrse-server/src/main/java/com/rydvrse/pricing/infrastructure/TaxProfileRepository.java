package com.rydvrse.pricing.infrastructure;

import com.rydvrse.pricing.domain.TaxProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface TaxProfileRepository extends JpaRepository<TaxProfileEntity, UUID> {

    List<TaxProfileEntity> findByCityIdAndStatusAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(UUID cityId, String status, OffsetDateTime effectiveFrom);
}
