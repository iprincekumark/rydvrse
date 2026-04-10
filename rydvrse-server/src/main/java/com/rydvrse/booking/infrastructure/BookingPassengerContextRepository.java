package com.rydvrse.booking.infrastructure;

import com.rydvrse.booking.domain.BookingPassengerContextEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BookingPassengerContextRepository extends JpaRepository<BookingPassengerContextEntity, UUID> {

    Optional<BookingPassengerContextEntity> findByBookingId(UUID bookingId);
}
