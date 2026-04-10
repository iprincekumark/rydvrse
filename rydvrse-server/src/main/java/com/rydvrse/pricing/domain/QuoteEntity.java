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
@Table(name = "quote", schema = "commercial")
public class QuoteEntity extends AbstractAppendOnlyEntity {

    @Column(name = "customer_profile_id", nullable = false)
    private UUID customerProfileId;

    @Column(name = "city_id", nullable = false)
    private UUID cityId;

    @Column(name = "service_type", nullable = false)
    private String serviceType;

    @Column(name = "pickup_zone_id")
    private UUID pickupZoneId;

    @Column(name = "drop_zone_id")
    private UUID dropZoneId;

    @Column(name = "airport_zone_band_id")
    private UUID airportZoneBandId;

    @Column(name = "one_way_band_id")
    private UUID oneWayBandId;

    @Column(name = "pricing_plan_id", nullable = false)
    private UUID pricingPlanId;

    @Column(name = "tax_profile_id", nullable = false)
    private UUID taxProfileId;

    @Column(name = "scheduled_pickup_at", nullable = false)
    private OffsetDateTime scheduledPickupAt;

    @Column(name = "quoted_duration_minutes")
    private Integer quotedDurationMinutes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "pickup_payload", nullable = false, columnDefinition = "jsonb")
    private JsonNode pickupPayload;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "drop_payload", columnDefinition = "jsonb")
    private JsonNode dropPayload;

    @Column(name = "currency_code", nullable = false)
    private String currencyCode;

    @Column(name = "subtotal_paise", nullable = false)
    private long subtotalPaise;

    @Column(name = "tax_paise", nullable = false)
    private long taxPaise;

    @Column(name = "total_paise", nullable = false)
    private long totalPaise;

    @Column(name = "quote_status", nullable = false)
    private String quoteStatus;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "requested_at", nullable = false)
    private OffsetDateTime requestedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", nullable = false, columnDefinition = "jsonb")
    private JsonNode metadata;

    public UUID getCustomerProfileId() {
        return customerProfileId;
    }

    public void setCustomerProfileId(UUID customerProfileId) {
        this.customerProfileId = customerProfileId;
    }

    public UUID getCityId() {
        return cityId;
    }

    public void setCityId(UUID cityId) {
        this.cityId = cityId;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public UUID getPickupZoneId() {
        return pickupZoneId;
    }

    public void setPickupZoneId(UUID pickupZoneId) {
        this.pickupZoneId = pickupZoneId;
    }

    public UUID getDropZoneId() {
        return dropZoneId;
    }

    public void setDropZoneId(UUID dropZoneId) {
        this.dropZoneId = dropZoneId;
    }

    public UUID getAirportZoneBandId() {
        return airportZoneBandId;
    }

    public void setAirportZoneBandId(UUID airportZoneBandId) {
        this.airportZoneBandId = airportZoneBandId;
    }

    public UUID getOneWayBandId() {
        return oneWayBandId;
    }

    public void setOneWayBandId(UUID oneWayBandId) {
        this.oneWayBandId = oneWayBandId;
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

    public JsonNode getPickupPayload() {
        return pickupPayload;
    }

    public void setPickupPayload(JsonNode pickupPayload) {
        this.pickupPayload = pickupPayload;
    }

    public JsonNode getDropPayload() {
        return dropPayload;
    }

    public void setDropPayload(JsonNode dropPayload) {
        this.dropPayload = dropPayload;
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

    public String getQuoteStatus() {
        return quoteStatus;
    }

    public void setQuoteStatus(String quoteStatus) {
        this.quoteStatus = quoteStatus;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(OffsetDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public OffsetDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(OffsetDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public JsonNode getMetadata() {
        return metadata;
    }

    public void setMetadata(JsonNode metadata) {
        this.metadata = metadata;
    }
}
