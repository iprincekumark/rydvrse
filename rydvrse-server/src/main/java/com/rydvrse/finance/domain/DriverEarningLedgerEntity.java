package com.rydvrse.finance.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.rydvrse.common.persistence.AbstractAppendOnlyEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "driver_earning_ledger", schema = "finance")
public class DriverEarningLedgerEntity extends AbstractAppendOnlyEntity {

    @Column(name = "driver_profile_id", nullable = false)
    private UUID driverProfileId;

    @Column(name = "trip_id", nullable = false, unique = true)
    private UUID tripId;

    @Column(name = "assignment_id", nullable = false)
    private UUID assignmentId;

    @Column(name = "payout_preview_id")
    private UUID payoutPreviewId;

    @Column(name = "ledger_status", nullable = false)
    private String ledgerStatus;

    @Column(name = "gross_payout_paise", nullable = false)
    private long grossPayoutPaise;

    @Column(name = "adjustment_paise", nullable = false)
    private long adjustmentPaise;

    @Column(name = "net_payout_paise", nullable = false)
    private long netPayoutPaise;

    @Column(name = "locked_at")
    private OffsetDateTime lockedAt;

    @Column(name = "settled_at")
    private OffsetDateTime settledAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "snapshot_payload", nullable = false, columnDefinition = "jsonb")
    private JsonNode snapshotPayload;

    public UUID getDriverProfileId() {
        return driverProfileId;
    }

    public void setDriverProfileId(UUID driverProfileId) {
        this.driverProfileId = driverProfileId;
    }

    public UUID getTripId() {
        return tripId;
    }

    public void setTripId(UUID tripId) {
        this.tripId = tripId;
    }

    public UUID getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(UUID assignmentId) {
        this.assignmentId = assignmentId;
    }

    public UUID getPayoutPreviewId() {
        return payoutPreviewId;
    }

    public void setPayoutPreviewId(UUID payoutPreviewId) {
        this.payoutPreviewId = payoutPreviewId;
    }

    public String getLedgerStatus() {
        return ledgerStatus;
    }

    public void setLedgerStatus(String ledgerStatus) {
        this.ledgerStatus = ledgerStatus;
    }

    public long getGrossPayoutPaise() {
        return grossPayoutPaise;
    }

    public void setGrossPayoutPaise(long grossPayoutPaise) {
        this.grossPayoutPaise = grossPayoutPaise;
    }

    public long getAdjustmentPaise() {
        return adjustmentPaise;
    }

    public void setAdjustmentPaise(long adjustmentPaise) {
        this.adjustmentPaise = adjustmentPaise;
    }

    public long getNetPayoutPaise() {
        return netPayoutPaise;
    }

    public void setNetPayoutPaise(long netPayoutPaise) {
        this.netPayoutPaise = netPayoutPaise;
    }

    public OffsetDateTime getLockedAt() {
        return lockedAt;
    }

    public void setLockedAt(OffsetDateTime lockedAt) {
        this.lockedAt = lockedAt;
    }

    public OffsetDateTime getSettledAt() {
        return settledAt;
    }

    public void setSettledAt(OffsetDateTime settledAt) {
        this.settledAt = settledAt;
    }

    public JsonNode getSnapshotPayload() {
        return snapshotPayload;
    }

    public void setSnapshotPayload(JsonNode snapshotPayload) {
        this.snapshotPayload = snapshotPayload;
    }
}
