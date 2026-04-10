package com.rydvrse.master.domain;

import com.rydvrse.common.persistence.AbstractAppendOnlyEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "airport_zone_band", schema = "master")
public class AirportZoneBandEntity extends AbstractAppendOnlyEntity {

    @Column(name = "city_id", nullable = false)
    private UUID cityId;

    @Column(name = "band_code", nullable = false)
    private String bandCode;

    @Column(name = "band_name", nullable = false)
    private String bandName;

    @Column(name = "service_zone_id", nullable = false)
    private UUID serviceZoneId;

    @Column(name = "airport_code", nullable = false)
    private String airportCode;

    public UUID getCityId() {
        return cityId;
    }

    public void setCityId(UUID cityId) {
        this.cityId = cityId;
    }

    public String getBandCode() {
        return bandCode;
    }

    public void setBandCode(String bandCode) {
        this.bandCode = bandCode;
    }

    public String getBandName() {
        return bandName;
    }

    public void setBandName(String bandName) {
        this.bandName = bandName;
    }

    public UUID getServiceZoneId() {
        return serviceZoneId;
    }

    public void setServiceZoneId(UUID serviceZoneId) {
        this.serviceZoneId = serviceZoneId;
    }

    public String getAirportCode() {
        return airportCode;
    }

    public void setAirportCode(String airportCode) {
        this.airportCode = airportCode;
    }
}
