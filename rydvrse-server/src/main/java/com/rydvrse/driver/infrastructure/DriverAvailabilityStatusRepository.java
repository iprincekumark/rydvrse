package com.rydvrse.driver.infrastructure;

import com.rydvrse.driver.domain.DriverAvailabilityStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface DriverAvailabilityStatusRepository extends JpaRepository<DriverAvailabilityStatusEntity, UUID> {

    List<DriverAvailabilityStatusEntity> findByDriverProfileIdIn(Collection<UUID> driverProfileIds);
}
