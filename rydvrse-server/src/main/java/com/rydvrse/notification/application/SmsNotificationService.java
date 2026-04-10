package com.rydvrse.notification.application;

import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class SmsNotificationService {

    public PushNotificationService.DeliveryResult send(String targetAddress, String body) {
        if (targetAddress == null || targetAddress.isBlank()) {
            return PushNotificationService.DeliveryResult.failed("SMS_TARGET_MISSING");
        }
        return PushNotificationService.DeliveryResult.sent("sms-" + OffsetDateTime.now().toEpochSecond());
    }
}
