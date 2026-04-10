package com.rydvrse.pricing.domain;

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
@Table(name = "booking_fare_snapshot", schema = "commercial")
public class BookingFareSnapshotEntity extends AbstractAppendOnlyEntity {

    @Column(name = "booking_id", nullable = false, unique = true)
    private UUID bookingId;

    @Column(name = "quote_id")
    private UUID quoteId;

    @Column(name = "pricing_plan_id", nullable = false)
    private UUID pricingPlanId;

    @Column(name = "tax_profile_id", nullable = false)
    private UUID taxProfileId;

    @Column(name = "service_type", nullable = false)
    private String serviceType;

    @Column(name = "scheduled_pickup_at", nullable = false)
    private OffsetDateTime scheduledPickupAt;

    @Column(name = "quoted_duration_minutes")
    private Integer quotedDurationMinutes;

    @Column(name = "subtotal_paise", nullable = false)
    private long subtotalPaise;

    @Column(name = "tax_paise", nullable = false)
    private long taxPaise;

    @Column(name = "total_paise", nullable = false)
    private long totalPaise;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "snapshot_payload", nullable = false, columnDefinition = "jsonb")
    private JsonNode snapshotPayload;

    public UUID getBookingId() {
        return bookingId;
    }

    public void setBookingId(UUID bookingId) {
        this.bookingId = bookingId;
    }

    public UUID getQuoteId() {
        return quoteId;
    }

    public void setQuoteId(UUID quoteId) {
        this.quoteId = quoteId;
    }

    public UUID getPricingPlanId() {
        return pricingPlanId;
    }

    public void setPricingPlanId(UUID pricingPlanId) {
        this.pricingPlanId = pricingPlanId;
    }

    public UUID getTaxProfileId() {
        return taxProfileId;
    }

    public void setTaxProfileId(UUID taxProfileId) {
        this.taxProfileId = taxProfileId;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public OffsetDateTime getScheduledPickupAt() {
        return scheduledPickupAt;
    }

    public void setScheduledPickupAt(OffsetDateTime scheduledPickupAt) {
        this.scheduledPickupAt = scheduledPickupAt;
    }

    public Integer getQuotedDurationMinutes() {
        return quotedDurationMinutes;
    }

    public void setQuotedDurationMinutes(Integer quotedDurationMinutes) {
        this.quotedDurationMinutes = quotedDurationMinutes;
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

    public JsonNode getSnapshotPayload() {
        return snapshotPayload;
    }

    public void setSnapshotPayload(JsonNode snapshotPayload) {
        this.snapshotPayload = snapshotPayload;
    }
}
