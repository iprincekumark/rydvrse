package com.rydvrse.trip.domain;

import com.rydvrse.common.persistence.AbstractVersionedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "trip", schema = "trip")
public class TripEntity extends AbstractVersionedEntity {

    @Column(name = "booking_id", nullable = false, unique = true)
    private UUID bookingId;

    @Column(name = "assignment_id", nullable = false)
    private UUID assignmentId;

    @Column(name = "driver_profile_id", nullable = false)
    private UUID driverProfileId;

    @Column(name = "customer_profile_id", nullable = false)
    private UUID customerProfileId;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "arrival_marked_at")
    private OffsetDateTime arrivalMarkedAt;

    @Column(name = "customer_start_confirmed_at")
    private OffsetDateTime customerStartConfirmedAt;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "abandoned_at")
    private OffsetDateTime abandonedAt;

    @Column(name = "final_duration_minutes")
    private Integer finalDurationMinutes;

    @Column(name = "final_total_paise")
    private Long finalTotalPaise;

    @Column(name = "under_review_reason_code")
    private String underReviewReasonCode;

    public UUID getBookingId() {
        return bookingId;
    }

    public void setBookingId(UUID bookingId) {
        this.bookingId = bookingId;
    }

    public UUID getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(UUID assignmentId) {
        this.assignmentId = assignmentId;
    }

    public UUID getDriverProfileId() {
        return driverProfileId;
    }

    public void setDriverProfileId(UUID driverProfileId) {
        this.driverProfileId = driverProfileId;
    }

    public UUID getCustomerProfileId() {
        return customerProfileId;
    }

    public void setCustomerProfileId(UUID customerProfileId) {
        this.customerProfileId = customerProfileId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public OffsetDateTime getArrivalMarkedAt() {
        return arrivalMarkedAt;
    }

    public void setArrivalMarkedAt(OffsetDateTime arrivalMarkedAt) {
        this.arrivalMarkedAt = arrivalMarkedAt;
    }

    public OffsetDateTime getCustomerStartConfirmedAt() {
        return customerStartConfirmedAt;
    }

    public void setCustomerStartConfirmedAt(OffsetDateTime customerStartConfirmedAt) {
        this.customerStartConfirmedAt = customerStartConfirmedAt;
    }

    public OffsetDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(OffsetDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public OffsetDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(OffsetDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public OffsetDateTime getAbandonedAt() {
        return abandonedAt;
    }

    public void setAbandonedAt(OffsetDateTime abandonedAt) {
        this.abandonedAt = abandonedAt;
    }

    public Integer getFinalDurationMinutes() {
        return finalDurationMinutes;
    }

    public void setFinalDurationMinutes(Integer finalDurationMinutes) {
        this.finalDurationMinutes = finalDurationMinutes;
    }

    public Long getFinalTotalPaise() {
        return finalTotalPaise;
    }

    public void setFinalTotalPaise(Long finalTotalPaise) {
        this.finalTotalPaise = finalTotalPaise;
    }

    public String getUnderReviewReasonCode() {
        return underReviewReasonCode;
    }

    public void setUnderReviewReasonCode(String underReviewReasonCode) {
        this.underReviewReasonCode = underReviewReasonCode;
    }
}
