package com.rydvrse.trip.event;

import com.rydvrse.shared.event.BaseDomainEvent;
import lombok.Getter;
import java.util.UUID;

@Getter
public class TripStatusChangedEvent extends BaseDomainEvent {
    private final String previousStatus;
    private final String newStatus;
    private final UUID customerId;
    private final UUID driverId;

    public TripStatusChangedEvent(UUID tripId, String prevStatus, String newStatus,
                                   UUID customerId, UUID driverId) {
        super("TRIP_STATUS_CHANGED", tripId, "TRIP");
        this.previousStatus = prevStatus;
        this.newStatus = newStatus;
        this.customerId = customerId;
        this.driverId = driverId;
    }
}
