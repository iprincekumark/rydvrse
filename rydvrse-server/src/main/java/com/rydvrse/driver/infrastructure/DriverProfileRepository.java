package com.rydvrse.driver.infrastructure;

import com.rydvrse.driver.domain.DriverProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DriverProfileRepository extends JpaRepository<DriverProfileEntity, UUID> {

    Optional<DriverProfileEntity> findByUserAccountId(UUID userAccountId);
}
