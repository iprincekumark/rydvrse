package com.rydvrse.booking.infrastructure;

import com.rydvrse.booking.domain.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<BookingEntity, UUID> {

    Optional<BookingEntity> findByIdAndCustomerProfileId(UUID id, UUID customerProfileId);

    List<BookingEntity> findByCustomerProfileIdOrderByScheduledPickupAtDesc(UUID customerProfileId);

    List<BookingEntity> findByStatusInAndScheduledPickupAtBetween(List<String> statuses, OffsetDateTime from, OffsetDateTime to);

    List<BookingEntity> findByCurrentTripId(UUID currentTripId);

    List<BookingEntity> findByStatusIn(List<String> statuses);

    List<BookingEntity> findByCityId(UUID cityId);
}
