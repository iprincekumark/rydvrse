package com.rydvrse.safety.repository;

import com.rydvrse.safety.entity.SosAlert;
import com.rydvrse.shared.enums.SosStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface SosAlertRepository extends JpaRepository<SosAlert, UUID> {
    List<SosAlert> findByStatus(SosStatus status);
    List<SosAlert> findByTripId(UUID tripId);
}
