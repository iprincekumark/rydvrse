package com.rydvrse.trip.infrastructure;

import com.rydvrse.trip.domain.HandoverChecklistEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface HandoverChecklistRepository extends JpaRepository<HandoverChecklistEntity, UUID> {

    Optional<HandoverChecklistEntity> findByTripId(UUID tripId);
}
