package com.rydvrse.auth.domain;

import com.rydvrse.common.persistence.AbstractTimestampedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "device_registration", schema = "iam")
public class DeviceRegistrationEntity extends AbstractTimestampedEntity {

    @Column(name = "user_account_id", nullable = false)
    private UUID userAccountId;

    @Column(name = "device_type", nullable = false)
    private String deviceType;

    @Column(name = "push_token", nullable = false)
    private String pushToken;

    @Column(name = "app_variant", nullable = false)
    private String appVariant;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "last_seen_at")
    private OffsetDateTime lastSeenAt;

    @Column(name = "revoked_at")
    private OffsetDateTime revokedAt;

    public UUID getUserAccountId() {
        return userAccountId;
    }

    public void setUserAccountId(UUID userAccountId) {
        this.userAccountId = userAccountId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getPushToken() {
        return pushToken;
    }

    public void setPushToken(String pushToken) {
        this.pushToken = pushToken;
    }

    public String getAppVariant() {
        return appVariant;
    }

    public void setAppVariant(String appVariant) {
        this.appVariant = appVariant;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public OffsetDateTime getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(OffsetDateTime lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public OffsetDateTime getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(OffsetDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }
}
