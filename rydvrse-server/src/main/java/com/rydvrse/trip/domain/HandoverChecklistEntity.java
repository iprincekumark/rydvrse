package com.rydvrse.trip.domain;

import com.rydvrse.common.persistence.AbstractAppendOnlyEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "handover_checklist", schema = "trip")
public class HandoverChecklistEntity extends AbstractAppendOnlyEntity {

    @Column(name = "trip_id", nullable = false, unique = true)
    private UUID tripId;

    @Column(name = "confirmed_driver_match", nullable = false)
    private boolean confirmedDriverMatch;

    @Column(name = "fuel_note")
    private String fuelNote;

    @Column(name = "instruction_note")
    private String instructionNote;

    @Column(name = "visible_concern_note")
    private String visibleConcernNote;

    @Column(name = "confirmed_by_customer_id", nullable = false)
    private UUID confirmedByCustomerId;

    @Column(name = "confirmed_at", nullable = false)
    private OffsetDateTime confirmedAt;

    public UUID getTripId() {
        return tripId;
    }

    public void setTripId(UUID tripId) {
        this.tripId = tripId;
    }

    public boolean isConfirmedDriverMatch() {
        return confirmedDriverMatch;
    }

    public void setConfirmedDriverMatch(boolean confirmedDriverMatch) {
        this.confirmedDriverMatch = confirmedDriverMatch;
    }

    public String getFuelNote() {
        return fuelNote;
    }

    public void setFuelNote(String fuelNote) {
        this.fuelNote = fuelNote;
    }

    public String getInstructionNote() {
        return instructionNote;
    }

    public void setInstructionNote(String instructionNote) {
        this.instructionNote = instructionNote;
    }

    public String getVisibleConcernNote() {
        return visibleConcernNote;
    }

    public void setVisibleConcernNote(String visibleConcernNote) {
        this.visibleConcernNote = visibleConcernNote;
    }

    public UUID getConfirmedByCustomerId() {
        return confirmedByCustomerId;
    }

    public void setConfirmedByCustomerId(UUID confirmedByCustomerId) {
        this.confirmedByCustomerId = confirmedByCustomerId;
    }

    public OffsetDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(OffsetDateTime confirmedAt) {
        this.confirmedAt = confirmedAt;
    }
}
