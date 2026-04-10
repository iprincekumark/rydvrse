package com.rydvrse.booking.application;

import com.rydvrse.booking.domain.BookingEntity;
import com.rydvrse.booking.domain.BookingStateLogEntity;
import com.rydvrse.booking.infrastructure.BookingStateLogRepository;
import com.rydvrse.common.security.CurrentActorService;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class BookingStateService {

    private final BookingStateLogRepository bookingStateLogRepository;
    private final CurrentActorService currentActorService;

    public BookingStateService(BookingStateLogRepository bookingStateLogRepository, CurrentActorService currentActorService) {
        this.bookingStateLogRepository = bookingStateLogRepository;
        this.currentActorService = currentActorService;
    }

    public void transition(BookingEntity booking, String newStatus, String reasonCode, String reasonNote) {
        BookingStateLogEntity log = new BookingStateLogEntity();
        log.setBookingId(booking.getId());
        log.setFromStatus(booking.getStatus());
        log.setToStatus(newStatus);
        log.setReasonCode(reasonCode);
        log.setReasonNote(reasonNote);
        log.setChangedByUserId(currentActorService.getCurrentActor().map(actor -> actor.userId()).orElse(null));
        log.setChangedAt(OffsetDateTime.now());
        bookingStateLogRepository.save(log);
        booking.setStatus(newStatus);
    }
}
