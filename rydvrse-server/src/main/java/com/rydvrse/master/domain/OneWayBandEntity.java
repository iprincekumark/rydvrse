package com.rydvrse.master.domain;

import com.rydvrse.common.persistence.AbstractAppendOnlyEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "one_way_band", schema = "master")
public class OneWayBandEntity extends AbstractAppendOnlyEntity {

    @Column(name = "city_id", nullable = false)
    private UUID cityId;

    @Column(name = "band_code", nullable = false)
    private String bandCode;

    @Column(name = "band_name", nullable = false)
    private String bandName;

    @Column(name = "source_zone_id", nullable = false)
    private UUID sourceZoneId;

    @Column(name = "destination_zone_id", nullable = false)
    private UUID destinationZoneId;

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

    public UUID getSourceZoneId() {
        return sourceZoneId;
    }

    public void setSourceZoneId(UUID sourceZoneId) {
        this.sourceZoneId = sourceZoneId;
    }

    public UUID getDestinationZoneId() {
        return destinationZoneId;
    }

    public void setDestinationZoneId(UUID destinationZoneId) {
        this.destinationZoneId = destinationZoneId;
    }
}
