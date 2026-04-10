package com.rydvrse.finance.domain;

import com.rydvrse.common.persistence.AbstractAppendOnlyEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "refund_decision", schema = "finance")
public class RefundDecisionEntity extends AbstractAppendOnlyEntity {

    @Column(name = "refund_request_id", nullable = false, unique = true)
    private UUID refundRequestId;

    @Column(name = "decision_status", nullable = false)
    private String decisionStatus;

    @Column(name = "approved_amount_paise")
    private Long approvedAmountPaise;

    @Column(name = "decided_by_user_id", nullable = false)
    private UUID decidedByUserId;

    @Column(name = "decision_reason_code", nullable = false)
    private String decisionReasonCode;

    @Column(name = "decision_note")
    private String decisionNote;

    @Column(name = "decided_at", nullable = false)
    private OffsetDateTime decidedAt;

    public UUID getRefundRequestId() {
        return refundRequestId;
    }

    public void setRefundRequestId(UUID refundRequestId) {
        this.refundRequestId = refundRequestId;
    }

    public String getDecisionStatus() {
        return decisionStatus;
    }

    public void setDecisionStatus(String decisionStatus) {
        this.decisionStatus = decisionStatus;
    }

    public Long getApprovedAmountPaise() {
        return approvedAmountPaise;
    }

    public void setApprovedAmountPaise(Long approvedAmountPaise) {
        this.approvedAmountPaise = approvedAmountPaise;
    }

    public UUID getDecidedByUserId() {
        return decidedByUserId;
    }

    public void setDecidedByUserId(UUID decidedByUserId) {
        this.decidedByUserId = decidedByUserId;
    }

    public String getDecisionReasonCode() {
        return decisionReasonCode;
    }

    public void setDecisionReasonCode(String decisionReasonCode) {
        this.decisionReasonCode = decisionReasonCode;
    }

    public String getDecisionNote() {
        return decisionNote;
    }

    public void setDecisionNote(String decisionNote) {
        this.decisionNote = decisionNote;
    }

    public OffsetDateTime getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(OffsetDateTime decidedAt) {
        this.decidedAt = decidedAt;
    }
}
