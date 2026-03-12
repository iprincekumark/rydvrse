package com.rydvrse.driver.event;

import com.rydvrse.shared.event.BaseDomainEvent;
import lombok.Getter;
import java.util.UUID;

@Getter
public class DriverAvailabilityChangedEvent extends BaseDomainEvent {
    private final boolean available;

    public DriverAvailabilityChangedEvent(UUID driverId, boolean available) {
        super("DRIVER_AVAILABILITY_CHANGED", driverId, "DRIVER");
        this.available = available;
    }
}
