package com.rydvrse.master.infrastructure;

import com.rydvrse.master.domain.ServiceabilityRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceabilityRuleRepository extends JpaRepository<ServiceabilityRuleEntity, UUID> {

    Optional<ServiceabilityRuleEntity> findByCityIdAndServiceZoneIdAndServiceType(UUID cityId, UUID serviceZoneId, String serviceType);

    List<ServiceabilityRuleEntity> findByCityIdAndEnabledTrue(UUID cityId);
}
