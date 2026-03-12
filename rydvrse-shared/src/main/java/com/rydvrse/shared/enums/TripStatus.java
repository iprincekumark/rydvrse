package com.rydvrse.shared.enums;

/**
 * Complete trip lifecycle states.
 *
 * State transitions:
 * REQUESTED → DRIVER_MATCHING → DRIVER_ASSIGNED → DRIVER_ARRIVING
 *           → TRIP_STARTED → TRIP_COMPLETED
 *
 * Any state can transition to CANCELLED (with restrictions after TRIP_STARTED).
 */
public enum TripStatus {
    REQUESTED,
    DRIVER_MATCHING,
    DRIVER_ASSIGNED,
    DRIVER_ARRIVING,
    TRIP_STARTED,
    TRIP_COMPLETED,
    CANCELLED
}
