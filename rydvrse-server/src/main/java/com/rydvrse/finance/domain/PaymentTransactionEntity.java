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
@Table(name = "payment_transaction", schema = "finance")
public class PaymentTransactionEntity extends AbstractAppendOnlyEntity {

    @Column(name = "payment_order_id", nullable = false)
    private UUID paymentOrderId;

    @Column(name = "provider_transaction_id")
    private String providerTransactionId;

    @Column(name = "transaction_type", nullable = false)
    private String transactionType;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "amount_paise", nullable = false)
    private long amountPaise;

    @Column(name = "provider_event_at")
    private OffsetDateTime providerEventAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "provider_payload", nullable = false, columnDefinition = "jsonb")
    private JsonNode providerPayload;

    @Column(name = "recorded_at", nullable = false)
    private OffsetDateTime recordedAt;

    public UUID getPaymentOrderId() {
        return paymentOrderId;
    }

    public void setPaymentOrderId(UUID paymentOrderId) {
        this.paymentOrderId = paymentOrderId;
    }

    public String getProviderTransactionId() {
        return providerTransactionId;
    }

    public void setProviderTransactionId(String providerTransactionId) {
        this.providerTransactionId = providerTransactionId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getAmountPaise() {
        return amountPaise;
    }

    public void setAmountPaise(long amountPaise) {
        this.amountPaise = amountPaise;
    }

    public OffsetDateTime getProviderEventAt() {
        return providerEventAt;
    }

    public void setProviderEventAt(OffsetDateTime providerEventAt) {
        this.providerEventAt = providerEventAt;
    }

    public JsonNode getProviderPayload() {
        return providerPayload;
    }

    public void setProviderPayload(JsonNode providerPayload) {
        this.providerPayload = providerPayload;
    }

    public OffsetDateTime getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(OffsetDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }
}
