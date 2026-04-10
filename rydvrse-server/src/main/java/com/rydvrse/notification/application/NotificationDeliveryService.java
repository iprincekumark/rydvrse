package com.rydvrse.notification.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rydvrse.notification.domain.NotificationDeliveryEntity;
import com.rydvrse.notification.domain.NotificationEventEntity;
import com.rydvrse.notification.domain.NotificationTemplateEntity;
import com.rydvrse.notification.infrastructure.NotificationDeliveryRepository;
import com.rydvrse.notification.infrastructure.NotificationEventRepository;
import com.rydvrse.notification.infrastructure.NotificationTemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Service
public class NotificationDeliveryService {

    private final NotificationDeliveryRepository notificationDeliveryRepository;
    private final NotificationEventRepository notificationEventRepository;
    private final NotificationTemplateRepository notificationTemplateRepository;
    private final TemplateRenderService templateRenderService;
    private final PushNotificationService pushNotificationService;
    private final SmsNotificationService smsNotificationService;
    private final ObjectMapper objectMapper;

    public NotificationDeliveryService(
            NotificationDeliveryRepository notificationDeliveryRepository,
            NotificationEventRepository notificationEventRepository,
            NotificationTemplateRepository notificationTemplateRepository,
            TemplateRenderService templateRenderService,
            PushNotificationService pushNotificationService,
            SmsNotificationService smsNotificationService,
            ObjectMapper objectMapper
    ) {
        this.notificationDeliveryRepository = notificationDeliveryRepository;
        this.notificationEventRepository = notificationEventRepository;
        this.notificationTemplateRepository = notificationTemplateRepository;
        this.templateRenderService = templateRenderService;
        this.pushNotificationService = pushNotificationService;
        this.smsNotificationService = smsNotificationService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public int dispatchQueuedDeliveries(int ignoredLimit) {
        List<NotificationDeliveryEntity> queued = notificationDeliveryRepository.findTop50ByStatusOrderByQueuedAtAsc("QUEUED");
        queued.forEach(this::sendDelivery);
        return queued.size();
    }

    @Transactional
    public int retryFailedDeliveries(int ignoredLimit) {
        List<NotificationDeliveryEntity> failed = notificationDeliveryRepository.findTop50ByStatusAndQueuedAtBeforeOrderByQueuedAtAsc(
                "FAILED",
                OffsetDateTime.now().minusMinutes(1)
        );
        failed.forEach(delivery -> {
            delivery.setStatus("QUEUED");
            delivery.setErrorCode(null);
            notificationDeliveryRepository.save(delivery);
            sendDelivery(delivery);
        });
        return failed.size();
    }

    private void sendDelivery(NotificationDeliveryEntity delivery) {
        NotificationEventEntity event = notificationEventRepository.findById(delivery.getNotificationEventId()).orElse(null);
        NotificationTemplateEntity template = delivery.getTemplateId() == null ? null : notificationTemplateRepository.findById(delivery.getTemplateId()).orElse(null);
        if (event == null || template == null) {
            delivery.setStatus("FAILED");
            delivery.setErrorCode("NOTIFICATION_CONTEXT_MISSING");
            notificationDeliveryRepository.save(delivery);
            return;
        }

        Map<String, Object> context = objectMapper.convertValue(event.getPayload(), new TypeReference<>() {
        });
        String subject = templateRenderService.render(template.getSubjectTemplate(), context);
        String body = templateRenderService.render(template.getBodyTemplate(), context);

        PushNotificationService.DeliveryResult result = "SMS".equals(delivery.getChannel())
                ? smsNotificationService.send(delivery.getTargetAddress(), body)
                : pushNotificationService.send(delivery.getTargetAddress(), subject, body);

        if (result.successful()) {
            delivery.setStatus("SENT");
            delivery.setProviderMessageId(result.providerMessageId());
            delivery.setSentAt(OffsetDateTime.now());
            delivery.setDeliveredAt(OffsetDateTime.now());
        } else {
            delivery.setStatus("FAILED");
            delivery.setErrorCode(result.errorCode());
        }
        notificationDeliveryRepository.save(delivery);
    }
}
