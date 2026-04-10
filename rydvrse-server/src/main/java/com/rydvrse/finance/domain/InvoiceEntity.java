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
@Table(name = "invoice", schema = "finance")
public class InvoiceEntity extends AbstractAppendOnlyEntity {

    @Column(name = "booking_id", nullable = false, unique = true)
    private UUID bookingId;

    @Column(name = "trip_id")
    private UUID tripId;

    @Column(name = "invoice_number", nullable = false, unique = true)
    private String invoiceNumber;

    @Column(name = "currency_code", nullable = false)
    private String currencyCode;

    @Column(name = "subtotal_paise", nullable = false)
    private long subtotalPaise;

    @Column(name = "tax_paise", nullable = false)
    private long taxPaise;

    @Column(name = "total_paise", nullable = false)
    private long totalPaise;

    @Column(name = "invoice_status", nullable = false)
    private String invoiceStatus;

    @Column(name = "issued_at", nullable = false)
    private OffsetDateTime issuedAt;

    @Column(name = "storage_key")
    private String storageKey;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "snapshot_payload", nullable = false, columnDefinition = "jsonb")
    private JsonNode snapshotPayload;

    public UUID getBookingId() {
        return bookingId;
    }

    public void setBookingId(UUID bookingId) {
        this.bookingId = bookingId;
    }

    public UUID getTripId() {
        return tripId;
    }

    public void setTripId(UUID tripId) {
        this.tripId = tripId;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public long getSubtotalPaise() {
        return subtotalPaise;
    }

    public void setSubtotalPaise(long subtotalPaise) {
        this.subtotalPaise = subtotalPaise;
    }

    public long getTaxPaise() {
        return taxPaise;
    }

    public void setTaxPaise(long taxPaise) {
        this.taxPaise = taxPaise;
    }

    public long getTotalPaise() {
        return totalPaise;
    }

    public void setTotalPaise(long totalPaise) {
        this.totalPaise = totalPaise;
    }

    public String getInvoiceStatus() {
        return invoiceStatus;
    }

    public void setInvoiceStatus(String invoiceStatus) {
        this.invoiceStatus = invoiceStatus;
    }

    public OffsetDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(OffsetDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public void setStorageKey(String storageKey) {
        this.storageKey = storageKey;
    }

    public JsonNode getSnapshotPayload() {
        return snapshotPayload;
    }

    public void setSnapshotPayload(JsonNode snapshotPayload) {
        this.snapshotPayload = snapshotPayload;
    }
}
