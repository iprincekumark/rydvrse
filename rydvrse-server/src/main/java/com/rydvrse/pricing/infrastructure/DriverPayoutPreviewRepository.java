package com.rydvrse.pricing.infrastructure;

import com.rydvrse.pricing.domain.DriverPayoutPreviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DriverPayoutPreviewRepository extends JpaRepository<DriverPayoutPreviewEntity, UUID> {

    Optional<DriverPayoutPreviewEntity> findByAssignmentId(UUID assignmentId);

    List<DriverPayoutPreviewEntity> findByDriverProfileIdOrderByCreatedAtDesc(UUID driverProfileId);
}
