package com.rydvrse.dispatch.domain;

import com.rydvrse.common.persistence.AbstractVersionedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "assignment", schema = "booking")
public class AssignmentEntity extends AbstractVersionedEntity {

    @Column(name = "booking_id", nullable = false)
    private UUID bookingId;

    @Column(name = "driver_profile_id")
    private UUID driverProfileId;

    @Column(name = "assignment_sequence_no", nullable = false)
    private int assignmentSequenceNo;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "is_current", nullable = false)
    private boolean current;

    @Column(name = "assigned_at")
    private OffsetDateTime assignedAt;

    @Column(name = "driver_eta_seconds")
    private Integer driverEtaSeconds;

    @Column(name = "risk_status", nullable = false)
    private String riskStatus;

    @Column(name = "rescue_required", nullable = false)
    private boolean rescueRequired;

    public UUID getBookingId() {
        return bookingId;
    }

    public void setBookingId(UUID bookingId) {
        this.bookingId = bookingId;
    }

    public UUID getDriverProfileId() {
        return driverProfileId;
    }

    public void setDriverProfileId(UUID driverProfileId) {
        this.driverProfileId = driverProfileId;
    }

    public int getAssignmentSequenceNo() {
        return assignmentSequenceNo;
    }

    public void setAssignmentSequenceNo(int assignmentSequenceNo) {
        this.assignmentSequenceNo = assignmentSequenceNo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isCurrent() {
        return current;
    }

    public void setCurrent(boolean current) {
        this.current = current;
    }

    public OffsetDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(OffsetDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public Integer getDriverEtaSeconds() {
        return driverEtaSeconds;
    }

    public void setDriverEtaSeconds(Integer driverEtaSeconds) {
        this.driverEtaSeconds = driverEtaSeconds;
    }

    public String getRiskStatus() {
        return riskStatus;
    }

    public void setRiskStatus(String riskStatus) {
        this.riskStatus = riskStatus;
    }

    public boolean isRescueRequired() {
        return rescueRequired;
    }

    public void setRescueRequired(boolean rescueRequired) {
        this.rescueRequired = rescueRequired;
    }
}
