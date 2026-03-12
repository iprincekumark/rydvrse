package com.rydvrse.analytics.service;

import com.rydvrse.auth.event.UserRegisteredEvent;
import com.rydvrse.payment.event.PaymentCompletedEvent;
import com.rydvrse.trip.event.TripCompletedEvent;
import com.rydvrse.trip.event.TripRequestedEvent;
import com.rydvrse.trip.event.TripStatusChangedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Analytics service — ingests domain events for business intelligence.
 *
 * In production: events flow through Kafka to a data pipeline
 * (e.g., Kafka → Spark/Flink → ClickHouse/BigQuery) for:
 * - Real-time dashboards (Grafana)
 * - Demand forecasting
 * - Driver performance analytics
 * - Revenue analytics
 * - City-level operational metrics
 */
@Slf4j
@Service
public class AnalyticsService {

    @EventListener @Async
    public void onUserRegistered(UserRegisteredEvent event) {
        log.info("[ANALYTICS] User registered: role={}, phone={}",
                event.getRole(), event.getPhoneNumber());
        // track("user.registered", Map.of("role", event.getRole().name()));
    }

    @EventListener @Async
    public void onTripRequested(TripRequestedEvent event) {
        log.info("[ANALYTICS] Trip requested: trip={}, city={}",
                event.getAggregateId(), event.getPickupCity());
        // track("trip.requested", Map.of("city", event.getPickupCity()));
    }

    @EventListener @Async
    public void onTripStatusChanged(TripStatusChangedEvent event) {
        log.info("[ANALYTICS] Trip status: trip={}, {} → {}",
                event.getAggregateId(), event.getPreviousStatus(), event.getNewStatus());
        // track("trip.status_changed", Map.of(...));
    }

    @EventListener @Async
    public void onTripCompleted(TripCompletedEvent event) {
        log.info("[ANALYTICS] Trip completed: trip={}, fare=₹{}, distance={}km",
                event.getAggregateId(), event.getFinalFare(), event.getDistanceKm());
        // track("trip.completed", Map.of("fare", event.getFinalFare(), "distance", event.getDistanceKm()));
    }

    @EventListener @Async
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        log.info("[ANALYTICS] Payment: amount=₹{}, driverPayout=₹{}, platformRevenue=₹{}",
                event.getAmount(), event.getDriverPayout(),
                event.getAmount() - event.getDriverPayout());
        // track("payment.completed", Map.of("revenue", event.getAmount() - event.getDriverPayout()));
    }
}
