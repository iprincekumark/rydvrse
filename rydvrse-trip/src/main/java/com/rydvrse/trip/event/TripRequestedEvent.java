package com.rydvrse.trip.event;

import com.rydvrse.shared.event.BaseDomainEvent;
import lombok.Getter;

import java.util.UUID;

@Getter
public class TripRequestedEvent extends BaseDomainEvent {
    private final UUID customerId;
    private final double pickupLat;
    private final double pickupLng;
    private final String pickupCity;

    public TripRequestedEvent(UUID tripId, UUID customerId, double pickupLat, double pickupLng, String pickupCity) {
        super("TRIP_REQUESTED", tripId, "TRIP");
        this.customerId = customerId;
        this.pickupLat = pickupLat;
        this.pickupLng = pickupLng;
        this.pickupCity = pickupCity;
    }
}
