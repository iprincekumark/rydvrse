package com.rydvrse.common.idempotency;

import com.rydvrse.common.error.ApiException;
import com.rydvrse.common.error.ErrorCode;
import com.rydvrse.common.util.HashingUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class IdempotencyService {

    private final IdempotencyKeyRepository idempotencyKeyRepository;

    public IdempotencyService(IdempotencyKeyRepository idempotencyKeyRepository) {
        this.idempotencyKeyRepository = idempotencyKeyRepository;
    }

    @Transactional
    public Optional<StoredResponseReference> checkExisting(String scope, String key, String payloadHash) {
        return idempotencyKeyRepository.findByRequestScopeAndIdempotencyKey(scope, key)
                .map(existing -> {
                    if (!existing.getRequestHash().equals(payloadHash)) {
                        throw ApiException.conflict(
                                ErrorCode.IDEMPOTENCY_KEY_REUSED_WITH_DIFFERENT_PAYLOAD,
                                "Idempotency key already used with a different payload"
                        );
                    }
                    return new StoredResponseReference(
                            existing.getResponseReferenceType(),
                            existing.getResponseReferenceId(),
                            existing.getStatus()
                    );
                });
    }

    @Transactional
    public void store(String scope, String key, Object payload, String responseReferenceType, UUID responseReferenceId, String status) {
        String requestHash = HashingUtils.sha256(String.valueOf(payload));
        IdempotencyKeyEntity entity = idempotencyKeyRepository.findByRequestScopeAndIdempotencyKey(scope, key)
                .orElseGet(IdempotencyKeyEntity::new);
        entity.setIdempotencyKey(key);
        entity.setRequestScope(scope);
        entity.setRequestHash(requestHash);
        entity.setResponseReferenceType(responseReferenceType);
        entity.setResponseReferenceId(responseReferenceId);
        entity.setStatus(status);
        entity.setExpiresAt(OffsetDateTime.now().plusDays(1));
        idempotencyKeyRepository.save(entity);
    }

    public String hashPayload(Object payload) {
        return HashingUtils.sha256(String.valueOf(payload));
    }

    public record StoredResponseReference(String responseReferenceType, UUID responseReferenceId, String status) {
    }
}
