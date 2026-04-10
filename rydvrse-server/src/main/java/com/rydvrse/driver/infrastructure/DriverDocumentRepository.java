package com.rydvrse.driver.infrastructure;

import com.rydvrse.driver.domain.DriverDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DriverDocumentRepository extends JpaRepository<DriverDocumentEntity, UUID> {

    List<DriverDocumentEntity> findByDriverProfileIdOrderBySubmittedAtDesc(UUID driverProfileId);
}
