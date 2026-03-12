package com.rydvrse.payment.event;

import com.rydvrse.shared.event.BaseDomainEvent;
import lombok.Getter;
import java.util.UUID;

@Getter
public class PaymentCompletedEvent extends BaseDomainEvent {
    private final UUID tripId;
    private final UUID customerId;
    private final UUID driverId;
    private final double amount;
    private final double driverPayout;

    public PaymentCompletedEvent(UUID paymentId, UUID tripId, UUID customerId,
                                  UUID driverId, double amount, double driverPayout) {
        super("PAYMENT_COMPLETED", paymentId, "PAYMENT");
        this.tripId = tripId;
        this.customerId = customerId;
        this.driverId = driverId;
        this.amount = amount;
        this.driverPayout = driverPayout;
    }
}
