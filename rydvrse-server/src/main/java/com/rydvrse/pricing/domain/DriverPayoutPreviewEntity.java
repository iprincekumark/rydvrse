package com.rydvrse.pricing.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.rydvrse.common.persistence.AbstractAppendOnlyEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "driver_payout_preview", schema = "commercial")
public class DriverPayoutPreviewEntity extends AbstractAppendOnlyEntity {

    @Column(name = "assignment_id", nullable = false, unique = true)
    private UUID assignmentId;

    @Column(name = "driver_profile_id", nullable = false)
    private UUID driverProfileId;

    @Column(name = "payout_plan_id", nullable = false)
    private UUID payoutPlanId;

    @Column(name = "service_type", nullable = false)
    private String serviceType;

    @Column(name = "preview_total_paise", nullable = false)
    private long previewTotalPaise;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "snapshot_payload", nullable = false, columnDefinition = "jsonb")
    private JsonNode snapshotPayload;

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

    public UUID getPayoutPlanId() {
        return payoutPlanId;
    }

    public void setPayoutPlanId(UUID payoutPlanId) {
        this.payoutPlanId = payoutPlanId;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public long getPreviewTotalPaise() {
        return previewTotalPaise;
    }

    public void setPreviewTotalPaise(long previewTotalPaise) {
        this.previewTotalPaise = previewTotalPaise;
    }

    public JsonNode getSnapshotPayload() {
        return snapshotPayload;
    }

    public void setSnapshotPayload(JsonNode snapshotPayload) {
        this.snapshotPayload = snapshotPayload;
    }
}
