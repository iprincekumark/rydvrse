package com.rydvrse.master.domain;

import com.rydvrse.common.persistence.AbstractVersionedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "serviceability_rule", schema = "master")
public class ServiceabilityRuleEntity extends AbstractVersionedEntity {

    @Column(name = "city_id", nullable = false)
    private UUID cityId;

    @Column(name = "service_zone_id", nullable = false)
    private UUID serviceZoneId;

    @Column(name = "service_type", nullable = false)
    private String serviceType;

    @Column(name = "is_enabled", nullable = false)
    private boolean enabled;

    @Column(name = "min_lead_minutes", nullable = false)
    private int minLeadMinutes;

    @Column(name = "operating_start_local")
    private LocalTime operatingStartLocal;

    @Column(name = "operating_end_local")
    private LocalTime operatingEndLocal;

    public UUID getCityId() {
        return cityId;
    }

    public void setCityId(UUID cityId) {
        this.cityId = cityId;
    }

    public UUID getServiceZoneId() {
        return serviceZoneId;
    }

    public void setServiceZoneId(UUID serviceZoneId) {
        this.serviceZoneId = serviceZoneId;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getMinLeadMinutes() {
        return minLeadMinutes;
    }

    public void setMinLeadMinutes(int minLeadMinutes) {
        this.minLeadMinutes = minLeadMinutes;
    }

    public LocalTime getOperatingStartLocal() {
        return operatingStartLocal;
    }

    public void setOperatingStartLocal(LocalTime operatingStartLocal) {
        this.operatingStartLocal = operatingStartLocal;
    }

    public LocalTime getOperatingEndLocal() {
        return operatingEndLocal;
    }

    public void setOperatingEndLocal(LocalTime operatingEndLocal) {
        this.operatingEndLocal = operatingEndLocal;
    }
}
