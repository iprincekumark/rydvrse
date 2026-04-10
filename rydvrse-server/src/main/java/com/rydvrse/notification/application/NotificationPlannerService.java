package com.rydvrse.notification.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.rydvrse.auth.domain.UserAccountEntity;
import com.rydvrse.auth.infrastructure.UserAccountRepository;
import com.rydvrse.booking.domain.BookingEntity;
import com.rydvrse.booking.infrastructure.BookingRepository;
import com.rydvrse.common.outbox.OutboxEventEntity;
import com.rydvrse.common.persistence.JsonNodeUtils;
import com.rydvrse.customer.domain.CustomerProfileEntity;
import com.rydvrse.customer.infrastructure.CustomerProfileRepository;
import com.rydvrse.notification.domain.NotificationDeliveryEntity;
import com.rydvrse.notification.domain.NotificationEventEntity;
import com.rydvrse.notification.domain.NotificationTemplateEntity;
import com.rydvrse.notification.infrastructure.NotificationDeliveryRepository;
import com.rydvrse.notification.infrastructure.NotificationEventRepository;
import com.rydvrse.notification.infrastructure.NotificationTemplateRepository;
import com.rydvrse.support.domain.SupportTicketEntity;
import com.rydvrse.support.infrastructure.SupportTicketRepository;
import com.rydvrse.trip.domain.TripEntity;
import com.rydvrse.trip.infrastructure.TripRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class NotificationPlannerService {

    private final NotificationEventRepository notificationEventRepository;
    private final NotificationDeliveryRepository notificationDeliveryRepository;
    private final NotificationTemplateRepository notificationTemplateRepository;
    private final NotificationPreferenceService notificationPreferenceService;
    private final BookingRepository bookingRepository;
    private final TripRepository tripRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final UserAccountRepository userAccountRepository;
    private final JsonNodeUtils jsonNodeUtils;

    public NotificationPlannerService(
            NotificationEventRepository notificationEventRepository,
            NotificationDeliveryRepository notificationDeliveryRepository,
            NotificationTemplateRepository notificationTemplateRepository,
            NotificationPreferenceService notificationPreferenceService,
            BookingRepository bookingRepository,
            TripRepository tripRepository,
            SupportTicketRepository supportTicketRepository,
            CustomerProfileRepository customerProfileRepository,
            UserAccountRepository userAccountRepository,
            JsonNodeUtils jsonNodeUtils
    ) {
        this.notificationEventRepository = notificationEventRepository;
        this.notificationDeliveryRepository = notificationDeliveryRepository;
        this.notificationTemplateRepository = notificationTemplateRepository;
        this.notificationPreferenceService = notificationPreferenceService;
        this.bookingRepository = bookingRepository;
        this.tripRepository = tripRepository;
        this.supportTicketRepository = supportTicketRepository;
        this.customerProfileRepository = customerProfileRepository;
        this.userAccountRepository = userAccountRepository;
        this.jsonNodeUtils = jsonNodeUtils;
    }

    @Transactional
    public int planFromOutbox(OutboxEventEntity outboxEvent) {
        String templateCode = templateCode(outboxEvent.getEventType());
        if (templateCode == null) {
            return 0;
        }

        NotificationContext context = resolveContext(outboxEvent);
        if (context.targetUserAccount() == null) {
            return 0;
        }

        NotificationEventEntity event = new NotificationEventEntity();
        event.setEventType(outboxEvent.getEventType());
        event.setUserAccountId(context.targetUserAccount().getId());
        event.setBookingId(context.bookingId());
        event.setTripId(context.tripId());
        event.setPayload(jsonNodeUtils.toJsonNode(context.payload()));
        event = notificationEventRepository.save(event);

        int deliveries = 0;
        for (String channel : notificationPreferenceService.preferredChannels(outboxEvent.getEventType())) {
            Optional<NotificationTemplateEntity> template = notificationTemplateRepository.findByTemplateCodeAndChannelAndLanguageCodeAndActiveTrue(
                    templateCode,
                    channel,
                    "en"
            );
            if (template.isEmpty()) {
                continue;
            }

            NotificationDeliveryEntity delivery = new NotificationDeliveryEntity();
            delivery.setNotificationEventId(event.getId());
            delivery.setChannel(channel);
            delivery.setTargetAddress(targetAddress(channel, context.targetUserAccount()));
            delivery.setTemplateId(template.get().getId());
            delivery.setStatus("QUEUED");
            delivery.setQueuedAt(OffsetDateTime.now());
            notificationDeliveryRepository.save(delivery);
            deliveries++;
        }
        return deliveries;
    }

    private NotificationContext resolveContext(OutboxEventEntity outboxEvent) {
        JsonNode payload = outboxEvent.getPayload();
        UUID bookingId = optionalUuid(payload, "booking_id");
        UUID tripId = optionalUuid(payload, "trip_id");
        UserAccountEntity userAccount = null;
        Map<String, Object> context = new LinkedHashMap<>();

        if (bookingId != null) {
            BookingEntity booking = bookingRepository.findById(bookingId).orElse(null);
            if (booking != null) {
                userAccount = customerProfileRepository.findById(booking.getCustomerProfileId())
                        .map(CustomerProfileEntity::getUserAccountId)
                        .flatMap(userAccountRepository::findById)
                        .orElse(null);
                context.put("scheduled_pickup_at", booking.getScheduledPickupAt());
                context.put("booking_id", booking.getId());
            }
        }

        if (tripId != null) {
            TripEntity trip = tripRepository.findById(tripId).orElse(null);
            if (trip != null && userAccount == null) {
                userAccount = customerProfileRepository.findById(trip.getCustomerProfileId())
                        .map(CustomerProfileEntity::getUserAccountId)
                        .flatMap(userAccountRepository::findById)
                        .orElse(null);
            }
            context.put("trip_id", tripId);
        }

        if ("SupportTicketCreatedEvent".equals(outboxEvent.getEventType())) {
            UUID ticketId = optionalUuid(payload, "ticket_id");
            SupportTicketEntity ticket = ticketId == null ? null : supportTicketRepository.findById(ticketId).orElse(null);
            if (ticket != null) {
                if (userAccount == null && ticket.getCustomerProfileId() != null) {
                    userAccount = customerProfileRepository.findById(ticket.getCustomerProfileId())
                            .map(CustomerProfileEntity::getUserAccountId)
                            .flatMap(userAccountRepository::findById)
                            .orElse(null);
                }
                context.put("ticket_code", ticket.getTicketCode());
                if (bookingId == null) {
                    bookingId = ticket.getBookingId();
                }
                if (tripId == null) {
                    tripId = ticket.getTripId();
                }
            }
        }

        context.put("driver_name", payload != null && payload.hasNonNull("driver_name") ? payload.get("driver_name").asText() : "Your driver");
        return new NotificationContext(userAccount, bookingId, tripId, context);
    }

    private UUID optionalUuid(JsonNode payload, String key) {
        return payload != null && payload.hasNonNull(key) ? UUID.fromString(payload.get(key).asText()) : null;
    }

    private String templateCode(String eventType) {
        return switch (eventType) {
            case "BookingConfirmedEvent" -> "BOOKING_CONFIRMED";
            case "AssignmentLockedEvent", "AssignmentReassignedEvent" -> "ASSIGNMENT_LOCKED";
            case "TripStartedEvent" -> "TRIP_STARTED";
            case "TripCompletedEvent" -> "TRIP_COMPLETED";
            case "SupportTicketCreatedEvent" -> "SUPPORT_TICKET_CREATED";
            default -> null;
        };
    }

    private String targetAddress(String channel, UserAccountEntity account) {
        if ("SMS".equals(channel)) {
            return account.getMobileNumberE164();
        }
        return "user:" + account.getId();
    }

    private record NotificationContext(
            UserAccountEntity targetUserAccount,
            UUID bookingId,
            UUID tripId,
            Map<String, Object> payload
    ) {
    }
}
