package com.rydvrse.driver.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "driver_availability_status", schema = "driver")
public class DriverAvailabilityStatusEntity {

    @Id
    @Column(name = "driver_profile_id")
    private UUID driverProfileId;

    @Column(name = "current_status", nullable = false)
    private String currentStatus;

    @Column(name = "current_booking_id")
    private UUID currentBookingId;

    @Column(name = "current_assignment_id")
    private UUID currentAssignmentId;

    @Column(name = "current_trip_id")
    private UUID currentTripId;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "updated_by_user_id")
    private UUID updatedByUserId;

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = OffsetDateTime.now();
    }

    public UUID getDriverProfileId() {
        return driverProfileId;
    }

    public void setDriverProfileId(UUID driverProfileId) {
        this.driverProfileId = driverProfileId;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public UUID getCurrentBookingId() {
        return currentBookingId;
    }

    public void setCurrentBookingId(UUID currentBookingId) {
        this.currentBookingId = currentBookingId;
    }

    public UUID getCurrentAssignmentId() {
        return currentAssignmentId;
    }

    public void setCurrentAssignmentId(UUID currentAssignmentId) {
        this.currentAssignmentId = currentAssignmentId;
    }

    public UUID getCurrentTripId() {
        return currentTripId;
    }

    public void setCurrentTripId(UUID currentTripId) {
        this.currentTripId = currentTripId;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public UUID getUpdatedByUserId() {
        return updatedByUserId;
    }

    public void setUpdatedByUserId(UUID updatedByUserId) {
        this.updatedByUserId = updatedByUserId;
    }
}
