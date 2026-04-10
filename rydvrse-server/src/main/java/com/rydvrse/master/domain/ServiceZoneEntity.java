package com.rydvrse.master.domain;

import com.rydvrse.common.persistence.AbstractVersionedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "service_zone", schema = "master")
public class ServiceZoneEntity extends AbstractVersionedEntity {

    @Column(name = "city_id", nullable = false)
    private UUID cityId;

    @Column(name = "zone_code", nullable = false)
    private String zoneCode;

    @Column(name = "zone_name", nullable = false)
    private String zoneName;

    @Column(name = "zone_type", nullable = false)
    private String zoneType;

    @Column(name = "launch_status", nullable = false)
    private String launchStatus;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "service_types_enabled", columnDefinition = "text[]")
    private String[] serviceTypesEnabled;

    public UUID getCityId() {
        return cityId;
    }

    public void setCityId(UUID cityId) {
        this.cityId = cityId;
    }

    public String getZoneCode() {
        return zoneCode;
    }

    public void setZoneCode(String zoneCode) {
        this.zoneCode = zoneCode;
    }

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    public String getZoneType() {
        return zoneType;
    }

    public void setZoneType(String zoneType) {
        this.zoneType = zoneType;
    }

    public String getLaunchStatus() {
        return launchStatus;
    }

    public void setLaunchStatus(String launchStatus) {
        this.launchStatus = launchStatus;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String[] getServiceTypesEnabled() {
        return serviceTypesEnabled;
    }

    public void setServiceTypesEnabled(String[] serviceTypesEnabled) {
        this.serviceTypesEnabled = serviceTypesEnabled;
    }
}
