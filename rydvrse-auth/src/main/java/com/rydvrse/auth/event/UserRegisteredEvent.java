package com.rydvrse.auth.event;

import com.rydvrse.shared.enums.UserRole;
import com.rydvrse.shared.event.BaseDomainEvent;
import lombok.Getter;

import java.util.UUID;

/**
 * Published when a new user registers via OTP verification.
 * Consumed by:
 *   - User module (creates Customer profile if role=CUSTOMER)
 *   - Driver module (creates Driver profile if role=DRIVER)
 *   - Notification module (sends welcome message)
 *   - Analytics module (tracks registration event)
 */
@Getter
public class UserRegisteredEvent extends BaseDomainEvent {

    private final String phoneNumber;
    private final UserRole role;

    public UserRegisteredEvent(UUID authUserId, String phoneNumber, UserRole role) {
        super("USER_REGISTERED", authUserId, "AUTH");
        this.phoneNumber = phoneNumber;
        this.role = role;
    }
}
