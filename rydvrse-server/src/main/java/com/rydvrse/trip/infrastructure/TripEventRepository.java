package com.rydvrse.trip.infrastructure;

import com.rydvrse.trip.domain.TripEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TripEventRepository extends JpaRepository<TripEventEntity, UUID> {

    List<TripEventEntity> findByTripIdOrderByEventAtAsc(UUID tripId);
}
