package com.rydvrse.driver.repository;

import com.rydvrse.driver.entity.DriverDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DriverDocumentRepository extends JpaRepository<DriverDocument, UUID> {
    List<DriverDocument> findByDriverId(UUID driverId);
}
