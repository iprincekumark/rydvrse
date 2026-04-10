package com.rydvrse.booking.domain;

import com.rydvrse.common.persistence.AbstractAppendOnlyEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "booking_instruction", schema = "booking")
public class BookingInstructionEntity extends AbstractAppendOnlyEntity {

    @Column(name = "booking_id", nullable = false)
    private UUID bookingId;

    @Column(name = "instruction_type", nullable = false)
    private String instructionType;

    @Column(name = "instruction_text", nullable = false)
    private String instructionText;

    @Column(name = "created_by_user_id", nullable = false)
    private UUID createdByUserId;

    public UUID getBookingId() {
        return bookingId;
    }

    public void setBookingId(UUID bookingId) {
        this.bookingId = bookingId;
    }

    public String getInstructionType() {
        return instructionType;
    }

    public void setInstructionType(String instructionType) {
        this.instructionType = instructionType;
    }

    public String getInstructionText() {
        return instructionText;
    }

    public void setInstructionText(String instructionText) {
        this.instructionText = instructionText;
    }

    public UUID getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(UUID createdByUserId) {
        this.createdByUserId = createdByUserId;
    }
}
