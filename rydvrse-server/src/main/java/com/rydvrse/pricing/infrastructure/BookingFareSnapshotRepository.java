package com.rydvrse.pricing.infrastructure;

import com.rydvrse.pricing.domain.BookingFareSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BookingFareSnapshotRepository extends JpaRepository<BookingFareSnapshotEntity, UUID> {

    Optional<BookingFareSnapshotEntity> findByBookingId(UUID bookingId);
}
