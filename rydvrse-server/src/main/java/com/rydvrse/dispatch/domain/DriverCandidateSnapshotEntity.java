package com.rydvrse.dispatch.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.rydvrse.common.persistence.AbstractAppendOnlyEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "driver_candidate_snapshot", schema = "booking")
public class DriverCandidateSnapshotEntity extends AbstractAppendOnlyEntity {

    @Column(name = "assignment_id", nullable = false)
    private UUID assignmentId;

    @Column(name = "driver_profile_id", nullable = false)
    private UUID driverProfileId;

    @Column(name = "snapshot_rank", nullable = false)
    private int snapshotRank;

    @Column(name = "score", precision = 8, scale = 4)
    private BigDecimal score;

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

    public int getSnapshotRank() {
        return snapshotRank;
    }

    public void setSnapshotRank(int snapshotRank) {
        this.snapshotRank = snapshotRank;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public JsonNode getSnapshotPayload() {
        return snapshotPayload;
    }

    public void setSnapshotPayload(JsonNode snapshotPayload) {
        this.snapshotPayload = snapshotPayload;
    }
}
