package com.rydvrse.pricing.domain;

import com.rydvrse.common.persistence.AbstractAppendOnlyEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "pricing_rule", schema = "commercial")
public class PricingRuleEntity extends AbstractAppendOnlyEntity {

    @Column(name = "pricing_plan_id", nullable = false)
    private UUID pricingPlanId;

    @Column(name = "service_type", nullable = false)
    private String serviceType;

    @Column(name = "service_zone_id")
    private UUID serviceZoneId;

    @Column(name = "airport_zone_band_id")
    private UUID airportZoneBandId;

    @Column(name = "one_way_band_id")
    private UUID oneWayBandId;

    @Column(name = "lead_time_bucket")
    private String leadTimeBucket;

    @Column(name = "rule_type", nullable = false)
    private String ruleType;

    @Column(name = "amount_paise", nullable = false)
    private long amountPaise;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    public UUID getPricingPlanId() {
        return pricingPlanId;
    }

    public void setPricingPlanId(UUID pricingPlanId) {
        this.pricingPlanId = pricingPlanId;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public UUID getServiceZoneId() {
        return serviceZoneId;
    }

    public void setServiceZoneId(UUID serviceZoneId) {
        this.serviceZoneId = serviceZoneId;
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

    public String getLeadTimeBucket() {
        return leadTimeBucket;
    }

    public void setLeadTimeBucket(String leadTimeBucket) {
        this.leadTimeBucket = leadTimeBucket;
    }

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    public long getAmountPaise() {
        return amountPaise;
    }

    public void setAmountPaise(long amountPaise) {
        this.amountPaise = amountPaise;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
