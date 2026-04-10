package com.rydvrse.finance.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.rydvrse.common.persistence.AbstractAppendOnlyEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

@Entity
@Table(name = "refund_transaction", schema = "finance")
public class RefundTransactionEntity extends AbstractAppendOnlyEntity {

    @Column(name = "refund_request_id", nullable = false)
    private java.util.UUID refundRequestId;

    @Column(name = "provider_refund_id")
    private String providerRefundId;

    @Column(name = "amount_paise", nullable = false)
    private long amountPaise;

    @Column(name = "status", nullable = false)
    private String status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "provider_payload", nullable = false, columnDefinition = "jsonb")
    private JsonNode providerPayload;

    @Column(name = "processed_at")
    private OffsetDateTime processedAt;

    public java.util.UUID getRefundRequestId() {
        return refundRequestId;
    }

    public void setRefundRequestId(java.util.UUID refundRequestId) {
        this.refundRequestId = refundRequestId;
    }

    public String getProviderRefundId() {
        return providerRefundId;
    }

    public void setProviderRefundId(String providerRefundId) {
        this.providerRefundId = providerRefundId;
    }

    public long getAmountPaise() {
        return amountPaise;
    }

    public void setAmountPaise(long amountPaise) {
        this.amountPaise = amountPaise;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public JsonNode getProviderPayload() {
        return providerPayload;
    }

    public void setProviderPayload(JsonNode providerPayload) {
        this.providerPayload = providerPayload;
    }

    public OffsetDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(OffsetDateTime processedAt) {
        this.processedAt = processedAt;
    }
}
