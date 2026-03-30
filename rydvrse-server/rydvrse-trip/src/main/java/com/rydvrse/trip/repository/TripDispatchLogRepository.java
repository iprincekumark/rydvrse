package com.rydvrse.trip.repository;

import com.rydvrse.trip.entity.TripDispatchLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TripDispatchLogRepository extends JpaRepository<TripDispatchLog, UUID> {
}
