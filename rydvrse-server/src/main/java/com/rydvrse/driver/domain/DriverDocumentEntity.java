package com.rydvrse.driver.domain;

import com.rydvrse.common.persistence.AbstractTimestampedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "driver_document", schema = "driver")
public class DriverDocumentEntity extends AbstractTimestampedEntity {

    @Column(name = "driver_profile_id", nullable = false)
    private UUID driverProfileId;

    @Column(name = "document_type", nullable = false)
    private String documentType;

    @Column(name = "document_number_masked")
    private String documentNumberMasked;

    @Column(name = "storage_key", nullable = false)
    private String storageKey;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "issued_at")
    private LocalDate issuedAt;

    @Column(name = "expires_at")
    private LocalDate expiresAt;

    @Column(name = "submitted_at", nullable = false)
    private OffsetDateTime submittedAt;

    public UUID getDriverProfileId() {
        return driverProfileId;
    }

    public void setDriverProfileId(UUID driverProfileId) {
        this.driverProfileId = driverProfileId;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getDocumentNumberMasked() {
        return documentNumberMasked;
    }

    public void setDocumentNumberMasked(String documentNumberMasked) {
        this.documentNumberMasked = documentNumberMasked;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public void setStorageKey(String storageKey) {
        this.storageKey = storageKey;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDate issuedAt) {
        this.issuedAt = issuedAt;
    }

    public LocalDate getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDate expiresAt) {
        this.expiresAt = expiresAt;
    }

    public OffsetDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(OffsetDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
}
