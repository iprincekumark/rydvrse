package com.rydvrse.trip.domain;

import com.rydvrse.common.persistence.AbstractAppendOnlyEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "location_ping", schema = "trip")
public class LocationPingEntity extends AbstractAppendOnlyEntity {

    @Column(name = "tracking_session_id", nullable = false)
    private UUID trackingSessionId;

    @Column(name = "driver_profile_id", nullable = false)
    private UUID driverProfileId;

    @Column(name = "ping_at", nullable = false)
    private OffsetDateTime pingAt;

    @Column(name = "latitude", nullable = false)
    private BigDecimal latitude;

    @Column(name = "longitude", nullable = false)
    private BigDecimal longitude;

    @Column(name = "heading_degrees")
    private BigDecimal headingDegrees;

    @Column(name = "speed_kph")
    private BigDecimal speedKph;

    @Column(name = "accuracy_meters")
    private BigDecimal accuracyMeters;

    @Column(name = "source_type", nullable = false)
    private String sourceType;

    public UUID getTrackingSessionId() {
        return trackingSessionId;
    }

    public void setTrackingSessionId(UUID trackingSessionId) {
        this.trackingSessionId = trackingSessionId;
    }

    public UUID getDriverProfileId() {
        return driverProfileId;
    }

    public void setDriverProfileId(UUID driverProfileId) {
        this.driverProfileId = driverProfileId;
    }

    public OffsetDateTime getPingAt() {
        return pingAt;
    }

    public void setPingAt(OffsetDateTime pingAt) {
        this.pingAt = pingAt;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getHeadingDegrees() {
        return headingDegrees;
    }

    public void setHeadingDegrees(BigDecimal headingDegrees) {
        this.headingDegrees = headingDegrees;
    }

    public BigDecimal getSpeedKph() {
        return speedKph;
    }

    public void setSpeedKph(BigDecimal speedKph) {
        this.speedKph = speedKph;
    }

    public BigDecimal getAccuracyMeters() {
        return accuracyMeters;
    }

    public void setAccuracyMeters(BigDecimal accuracyMeters) {
        this.accuracyMeters = accuracyMeters;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }
}
