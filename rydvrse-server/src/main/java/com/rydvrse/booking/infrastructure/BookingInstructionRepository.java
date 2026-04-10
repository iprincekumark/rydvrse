package com.rydvrse.booking.infrastructure;

import com.rydvrse.booking.domain.BookingInstructionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BookingInstructionRepository extends JpaRepository<BookingInstructionEntity, UUID> {

    List<BookingInstructionEntity> findByBookingIdOrderByCreatedAtAsc(UUID bookingId);
}
