package com.rydvrse.trip.infrastructure;

import com.rydvrse.trip.domain.TripShareLinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TripShareLinkRepository extends JpaRepository<TripShareLinkEntity, UUID> {

    List<TripShareLinkEntity> findByTripIdOrderByCreatedAtDesc(UUID tripId);

    Optional<TripShareLinkEntity> findFirstByTripIdAndStatusOrderByCreatedAtDesc(UUID tripId, String status);
}
