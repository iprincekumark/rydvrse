package com.rydvrse.notification.application;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationPreferenceService {

    public List<String> preferredChannels(String eventType) {
        return switch (eventType) {
            case "SupportTicketCreatedEvent" -> List.of("SMS");
            case "BookingConfirmedEvent", "AssignmentLockedEvent", "TripStartedEvent", "TripCompletedEvent" -> List.of("PUSH", "SMS");
            default -> List.of("PUSH");
        };
    }
}
