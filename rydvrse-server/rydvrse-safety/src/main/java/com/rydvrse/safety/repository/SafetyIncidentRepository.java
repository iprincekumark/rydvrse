package com.rydvrse.safety.repository;

import com.rydvrse.safety.domain.SafetyIncident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface SafetyIncidentRepository extends JpaRepository<SafetyIncident, UUID> {
    List<SafetyIncident> findByTripId(UUID tripId);
    List<SafetyIncident> findByStatusOrderByCreatedAtDesc(String status);
}
