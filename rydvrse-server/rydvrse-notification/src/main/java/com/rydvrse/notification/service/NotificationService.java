package com.rydvrse.notification.service;

import com.rydvrse.notification.entity.DeviceToken;
import com.rydvrse.notification.entity.NotificationLog;
import com.rydvrse.notification.repository.DeviceTokenRepository;
import com.rydvrse.notification.repository.NotificationLogRepository;
import com.rydvrse.shared.enums.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

@Slf4j @Service @RequiredArgsConstructor
public class NotificationService {
    private final NotificationLogRepository logRepository;
    private final DeviceTokenRepository tokenRepository;
    private final StringRedisTemplate redisTemplate;

    private static final Duration DEDUP_TTL = Duration.ofMinutes(5);

    @Transactional
    public void send(UUID recipientId, UserType recipientType, NotificationChannel channel,
                     String type, String title, String body) {
        // Deduplication check
        String dedupKey = String.format("notif_dedup:%s:%s:%s", type, recipientId, channel);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(dedupKey))) {
            log.debug("Notification deduplicated: {}", dedupKey);
            return;
        }

        NotificationLog notifLog = NotificationLog.builder()
                .recipientId(recipientId).recipientType(recipientType)
                .channel(channel).type(type).title(title).body(body)
                .status(NotificationStatus.SENT).build();
        logRepository.save(notifLog);

        // In production: dispatch to FCM/Twilio/WebSocket based on channel
        log.info("Notification sent: {} → {} [{}] via {}", type, recipientId, title, channel);

        redisTemplate.opsForValue().set(dedupKey, "1", DEDUP_TTL);
    }

    @Transactional
    public DeviceToken registerToken(UUID userId, UserType userType, DevicePlatform platform, String fcmToken) {
        DeviceToken token = tokenRepository.findByUserIdAndPlatform(userId, platform)
                .orElse(DeviceToken.builder().userId(userId).userType(userType).platform(platform).build());
        token.setFcmToken(fcmToken);
        token.setIsActive(true);
        return tokenRepository.save(token);
    }
}
