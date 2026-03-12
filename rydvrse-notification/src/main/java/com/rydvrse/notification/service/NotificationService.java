package com.rydvrse.notification.service;

import com.rydvrse.payment.event.PaymentCompletedEvent;
import com.rydvrse.trip.event.TripCompletedEvent;
import com.rydvrse.trip.event.TripRequestedEvent;
import com.rydvrse.trip.event.TripStatusChangedEvent;
import com.rydvrse.auth.event.UserRegisteredEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Notification service — listens to domain events and sends notifications
 * via Push, SMS, Email, and In-App channels.
 *
 * In production: integrates with Firebase Cloud Messaging (push),
 * AWS SNS/Twilio (SMS), and SendGrid (email).
 */
@Slf4j
@Service
public class NotificationService {

    @EventListener
    @Async
    public void onUserRegistered(UserRegisteredEvent event) {
        log.info("[NOTIFICATION] Welcome SMS to {}: Welcome to RYDVRSE!", event.getPhoneNumber());
        // sendSms(event.getPhoneNumber(), "Welcome to RYDVRSE! Your Car. Our Driver. Your Destination.");
    }

    @EventListener
    @Async
    public void onTripRequested(TripRequestedEvent event) {
        log.info("[NOTIFICATION] Push to customer {}: Looking for nearby drivers...",
                event.getCustomerId());
        // sendPush(customerId, "Searching for drivers near you...");
    }

    @EventListener
    @Async
    public void onTripStatusChanged(TripStatusChangedEvent event) {
        switch (event.getNewStatus()) {
            case "DRIVER_ASSIGNED":
                log.info("[NOTIFICATION] Push to customer {}: Driver assigned!",
                        event.getCustomerId());
                log.info("[NOTIFICATION] Push to driver {}: New trip assigned!",
                        event.getDriverId());
                break;
            case "DRIVER_ARRIVING":
                log.info("[NOTIFICATION] Push to customer {}: Driver has arrived!",
                        event.getCustomerId());
                break;
            case "TRIP_STARTED":
                log.info("[NOTIFICATION] Push to customer {}: Trip has started!",
                        event.getCustomerId());
                break;
            case "CANCELLED":
                log.info("[NOTIFICATION] Push to customer {}: Trip cancelled",
                        event.getCustomerId());
                if (event.getDriverId() != null) {
                    log.info("[NOTIFICATION] Push to driver {}: Trip cancelled",
                            event.getDriverId());
                }
                break;
        }
    }

    @EventListener
    @Async
    public void onTripCompleted(TripCompletedEvent event) {
        log.info("[NOTIFICATION] Push to customer {}: Trip completed! Fare: ₹{}",
                event.getCustomerId(), event.getFinalFare());
        log.info("[NOTIFICATION] Push to driver {}: Trip completed! You earned for this trip",
                event.getDriverId());
    }

    @EventListener
    @Async
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        log.info("[NOTIFICATION] Push to customer {}: Payment of ₹{} processed",
                event.getCustomerId(), event.getAmount());
        log.info("[NOTIFICATION] Push to driver {}: ₹{} credited to wallet",
                event.getDriverId(), event.getDriverPayout());
    }
}
