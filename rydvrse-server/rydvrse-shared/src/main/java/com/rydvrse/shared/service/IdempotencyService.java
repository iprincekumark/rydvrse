package com.rydvrse.shared.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Redis-backed idempotency service for payment operations.
 * Prevents duplicate processing of the same operation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final StringRedisTemplate redisTemplate;
    private static final Duration IDEMPOTENCY_TTL = Duration.ofHours(24);
    private static final String KEY_PREFIX = "idempotency:";

    /**
     * Check if an idempotency key has been processed.
     */
    public boolean isDuplicate(String idempotencyKey) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + idempotencyKey));
    }

    /**
     * Mark an idempotency key as processed with the response.
     */
    public void markProcessed(String idempotencyKey, String response) {
        redisTemplate.opsForValue().set(KEY_PREFIX + idempotencyKey, response, IDEMPOTENCY_TTL);
    }

    /**
     * Get cached response for a processed idempotency key.
     */
    public String getCachedResponse(String idempotencyKey) {
        return redisTemplate.opsForValue().get(KEY_PREFIX + idempotencyKey);
    }
}
