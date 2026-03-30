package com.rydvrse.trip.repository;

import com.rydvrse.trip.entity.TripLocationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TripLocationLogRepository extends JpaRepository<TripLocationLog, UUID> {
    List<TripLocationLog> findByTripIdOrderByTimestampAsc(UUID tripId);
}
