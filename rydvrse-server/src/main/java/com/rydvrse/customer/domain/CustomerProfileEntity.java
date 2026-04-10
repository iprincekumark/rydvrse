package com.rydvrse.customer.domain;

import com.rydvrse.common.persistence.AbstractMutableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "customer_profile", schema = "customer")
public class CustomerProfileEntity extends AbstractMutableEntity {

    @Column(name = "user_account_id", nullable = false, unique = true)
    private UUID userAccountId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email", columnDefinition = "citext")
    private String email;

    @Column(name = "default_city_id", nullable = false)
    private UUID defaultCityId;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "last_active_at")
    private OffsetDateTime lastActiveAt;

    @Column(name = "trust_score")
    private BigDecimal trustScore;

    public UUID getUserAccountId() {
        return userAccountId;
    }

    public void setUserAccountId(UUID userAccountId) {
        this.userAccountId = userAccountId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UUID getDefaultCityId() {
        return defaultCityId;
    }

    public void setDefaultCityId(UUID defaultCityId) {
        this.defaultCityId = defaultCityId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public OffsetDateTime getLastActiveAt() {
        return lastActiveAt;
    }

    public void setLastActiveAt(OffsetDateTime lastActiveAt) {
        this.lastActiveAt = lastActiveAt;
    }

    public BigDecimal getTrustScore() {
        return trustScore;
    }

    public void setTrustScore(BigDecimal trustScore) {
        this.trustScore = trustScore;
    }
}
