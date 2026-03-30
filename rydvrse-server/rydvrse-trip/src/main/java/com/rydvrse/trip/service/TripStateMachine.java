package com.rydvrse.trip.service;

import com.rydvrse.shared.enums.TripStatus;
import com.rydvrse.shared.exception.InvalidStateTransitionException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * Trip state machine enforcing valid transitions per spec:
 * PENDING → SEARCHING_DRIVER → DRIVER_ASSIGNED → DRIVER_EN_ROUTE
 * → DRIVER_ARRIVED → IN_PROGRESS → COMPLETED
 * ↘ CANCELLED (from any pre-IN_PROGRESS state)
 */
@Component
public class TripStateMachine {

    private static final Map<TripStatus, Set<TripStatus>> VALID_TRANSITIONS = Map.of(
            TripStatus.PENDING, Set.of(TripStatus.SEARCHING_DRIVER, TripStatus.CANCELLED),
            TripStatus.SEARCHING_DRIVER, Set.of(TripStatus.DRIVER_ASSIGNED, TripStatus.CANCELLED),
            TripStatus.DRIVER_ASSIGNED, Set.of(TripStatus.DRIVER_EN_ROUTE, TripStatus.CANCELLED),
            TripStatus.DRIVER_EN_ROUTE, Set.of(TripStatus.DRIVER_ARRIVED, TripStatus.CANCELLED),
            TripStatus.DRIVER_ARRIVED, Set.of(TripStatus.IN_PROGRESS, TripStatus.CANCELLED),
            TripStatus.IN_PROGRESS, Set.of(TripStatus.COMPLETED)
    );

    public void validateTransition(TripStatus current, TripStatus target) {
        Set<TripStatus> validTargets = VALID_TRANSITIONS.getOrDefault(current, Set.of());
        if (!validTargets.contains(target)) {
            throw new InvalidStateTransitionException("Trip", current.name(), target.name());
        }
    }
}
