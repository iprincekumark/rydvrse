package com.rydvrse.booking.infrastructure;

import com.rydvrse.booking.domain.BookingStateLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BookingStateLogRepository extends JpaRepository<BookingStateLogEntity, UUID> {

    List<BookingStateLogEntity> findByBookingIdOrderByChangedAtAsc(UUID bookingId);
}
