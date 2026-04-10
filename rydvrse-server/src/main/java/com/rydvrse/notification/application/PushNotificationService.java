package com.rydvrse.notification.application;

import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class PushNotificationService {

    public DeliveryResult send(String targetAddress, String subject, String body) {
        if (targetAddress == null || targetAddress.isBlank()) {
            return DeliveryResult.failed("PUSH_TARGET_MISSING");
        }
        return DeliveryResult.sent("push-" + OffsetDateTime.now().toEpochSecond());
    }

    public record DeliveryResult(boolean successful, String providerMessageId, String errorCode) {
        public static DeliveryResult sent(String providerMessageId) {
            return new DeliveryResult(true, providerMessageId, null);
        }

        public static DeliveryResult failed(String errorCode) {
            return new DeliveryResult(false, null, errorCode);
        }
    }
}
