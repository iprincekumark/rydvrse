package com.rydvrse.dispatch.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.rydvrse.common.persistence.AbstractAppendOnlyEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "assignment_attempt", schema = "booking")
public class AssignmentAttemptEntity extends AbstractAppendOnlyEntity {

    @Column(name = "assignment_id", nullable = false)
    private UUID assignmentId;

    @Column(name = "driver_profile_id", nullable = false)
    private UUID driverProfileId;

    @Column(name = "attempt_status", nullable = false)
    private String attemptStatus;

    @Column(name = "offer_sequence_no", nullable = false)
    private int offerSequenceNo;

    @Column(name = "offered_at", nullable = false)
    private OffsetDateTime offeredAt;

    @Column(name = "responded_at")
    private OffsetDateTime respondedAt;

    @Column(name = "response_reason_code")
    private String responseReasonCode;

    @Column(name = "candidate_score", precision = 8, scale = 4)
    private BigDecimal candidateScore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "candidate_snapshot", nullable = false, columnDefinition = "jsonb")
    private JsonNode candidateSnapshot;

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

    public String getAttemptStatus() {
        return attemptStatus;
    }

    public void setAttemptStatus(String attemptStatus) {
        this.attemptStatus = attemptStatus;
    }

    public int getOfferSequenceNo() {
        return offerSequenceNo;
    }

    public void setOfferSequenceNo(int offerSequenceNo) {
        this.offerSequenceNo = offerSequenceNo;
    }

    public OffsetDateTime getOfferedAt() {
        return offeredAt;
    }

    public void setOfferedAt(OffsetDateTime offeredAt) {
        this.offeredAt = offeredAt;
    }

    public OffsetDateTime getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(OffsetDateTime respondedAt) {
        this.respondedAt = respondedAt;
    }

    public String getResponseReasonCode() {
        return responseReasonCode;
    }

    public void setResponseReasonCode(String responseReasonCode) {
        this.responseReasonCode = responseReasonCode;
    }

    public BigDecimal getCandidateScore() {
        return candidateScore;
    }

    public void setCandidateScore(BigDecimal candidateScore) {
        this.candidateScore = candidateScore;
    }

    public JsonNode getCandidateSnapshot() {
        return candidateSnapshot;
    }

    public void setCandidateSnapshot(JsonNode candidateSnapshot) {
        this.candidateSnapshot = candidateSnapshot;
    }
}
