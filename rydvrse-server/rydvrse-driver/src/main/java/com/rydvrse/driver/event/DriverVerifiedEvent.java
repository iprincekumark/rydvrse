package com.rydvrse.driver.event;

import com.rydvrse.shared.event.BaseDomainEvent;
import java.util.UUID;

public class DriverVerifiedEvent extends BaseDomainEvent {
    public DriverVerifiedEvent(UUID driverId) {
        super("DRIVER_VERIFIED", driverId, "DRIVER");
    }
}
