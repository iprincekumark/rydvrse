package com.rydvrse.trip.domain;

import com.rydvrse.common.persistence.AbstractAppendOnlyEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "trip_share_link", schema = "trip")
public class TripShareLinkEntity extends AbstractAppendOnlyEntity {

    @Column(name = "trip_id", nullable = false)
    private UUID tripId;

    @Column(name = "customer_profile_id", nullable = false)
    private UUID customerProfileId;

    @Column(name = "share_token_hash", nullable = false, unique = true)
    private String shareTokenHash;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "revoked_at")
    private OffsetDateTime revokedAt;

    public UUID getTripId() {
        return tripId;
    }

    public void setTripId(UUID tripId) {
        this.tripId = tripId;
    }

    public UUID getCustomerProfileId() {
        return customerProfileId;
    }

    public void setCustomerProfileId(UUID customerProfileId) {
        this.customerProfileId = customerProfileId;
    }

    public String getShareTokenHash() {
        return shareTokenHash;
    }

    public void setShareTokenHash(String shareTokenHash) {
        this.shareTokenHash = shareTokenHash;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(OffsetDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public OffsetDateTime getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(OffsetDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }
}
