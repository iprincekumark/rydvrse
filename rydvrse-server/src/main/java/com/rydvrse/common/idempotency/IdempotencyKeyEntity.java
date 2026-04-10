package com.rydvrse.common.idempotency;

import com.rydvrse.common.persistence.AbstractAppendOnlyEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "idempotency_key", schema = "audit")
public class IdempotencyKeyEntity extends AbstractAppendOnlyEntity {

    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    @Column(name = "request_scope", nullable = false)
    private String requestScope;

    @Column(name = "request_hash", nullable = false)
    private String requestHash;

    @Column(name = "response_reference_type")
    private String responseReferenceType;

    @Column(name = "response_reference_id")
    private UUID responseReferenceId;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getRequestScope() {
        return requestScope;
    }

    public void setRequestScope(String requestScope) {
        this.requestScope = requestScope;
    }

    public String getRequestHash() {
        return requestHash;
    }

    public void setRequestHash(String requestHash) {
        this.requestHash = requestHash;
    }

    public String getResponseReferenceType() {
        return responseReferenceType;
    }

    public void setResponseReferenceType(String responseReferenceType) {
        this.responseReferenceType = responseReferenceType;
    }

    public UUID getResponseReferenceId() {
        return responseReferenceId;
    }

    public void setResponseReferenceId(UUID responseReferenceId) {
        this.responseReferenceId = responseReferenceId;
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
}
