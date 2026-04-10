package com.rydvrse.auth.infrastructure;

import com.rydvrse.auth.domain.DeviceRegistrationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DeviceRegistrationRepository extends JpaRepository<DeviceRegistrationEntity, UUID> {

    Optional<DeviceRegistrationEntity> findByUserAccountIdAndDeviceId(UUID userAccountId, String deviceId);
}
