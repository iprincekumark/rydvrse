package com.rydvrse.master.infrastructure;

import com.rydvrse.master.domain.FeatureFlagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FeatureFlagRepository extends JpaRepository<FeatureFlagEntity, UUID> {

    List<FeatureFlagEntity> findByEnabledTrue();
}
