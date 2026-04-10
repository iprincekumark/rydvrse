package com.rydvrse.booking.domain;

import com.rydvrse.common.persistence.AbstractAppendOnlyEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "booking_passenger_context", schema = "booking")
public class BookingPassengerContextEntity extends AbstractAppendOnlyEntity {

    @Column(name = "booking_id", nullable = false)
    private UUID bookingId;

    @Column(name = "passenger_name", nullable = false)
    private String passengerName;

    @Column(name = "passenger_mobile_e164")
    private String passengerMobileE164;

    @Column(name = "is_self_booking", nullable = false)
    private boolean selfBooking;

    public UUID getBookingId() {
        return bookingId;
    }

    public void setBookingId(UUID bookingId) {
        this.bookingId = bookingId;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public String getPassengerMobileE164() {
        return passengerMobileE164;
    }

    public void setPassengerMobileE164(String passengerMobileE164) {
        this.passengerMobileE164 = passengerMobileE164;
    }

    public boolean isSelfBooking() {
        return selfBooking;
    }

    public void setSelfBooking(boolean selfBooking) {
        this.selfBooking = selfBooking;
    }
}
