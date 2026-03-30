package com.rydvrse.notification.repository;

import com.rydvrse.notification.entity.DeviceToken;
import com.rydvrse.shared.enums.DevicePlatform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, UUID> {
    Optional<DeviceToken> findByUserIdAndPlatform(UUID userId, DevicePlatform platform);
}
