package com.rydvrse.trip.event;

import com.rydvrse.shared.event.BaseDomainEvent;
import lombok.Getter;
import java.util.UUID;

@Getter
public class TripCompletedEvent extends BaseDomainEvent {
    private final UUID customerId;
    private final UUID driverId;
    private final double finalFare;
    private final double distanceKm;

    public TripCompletedEvent(UUID tripId, UUID customerId, UUID driverId,
                               double finalFare, double distanceKm) {
        super("TRIP_COMPLETED", tripId, "TRIP");
        this.customerId = customerId;
        this.driverId = driverId;
        this.finalFare = finalFare;
        this.distanceKm = distanceKm;
    }
}
