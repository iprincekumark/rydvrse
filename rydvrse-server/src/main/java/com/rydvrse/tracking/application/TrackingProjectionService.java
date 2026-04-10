package com.rydvrse.tracking.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Service
public class TrackingProjectionService {

    private static final Logger log = LoggerFactory.getLogger(TrackingProjectionService.class);

    private final ObjectProvider<StringRedisTemplate> redisTemplateProvider;
    private final ObjectMapper objectMapper;

    public TrackingProjectionService(ObjectProvider<StringRedisTemplate> redisTemplateProvider, ObjectMapper objectMapper) {
        this.redisTemplateProvider = redisTemplateProvider;
        this.objectMapper = objectMapper;
    }

    public void updateTripSnapshot(UUID tripId, Map<String, Object> snapshot) {
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate == null) {
            return;
        }
        try {
            redisTemplate.opsForValue().set("tracking:trip:" + tripId + ":latest", objectMapper.writeValueAsString(snapshot), Duration.ofMinutes(30));
        } catch (JsonProcessingException ex) {
            log.warn("Unable to serialize tracking snapshot for trip {}", tripId, ex);
        } catch (RuntimeException ex) {
            log.warn("Unable to persist tracking snapshot for trip {}", tripId, ex);
        }
    }
}
