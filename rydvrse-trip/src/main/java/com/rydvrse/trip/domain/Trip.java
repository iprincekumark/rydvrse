package com.rydvrse.trip.domain;

import com.rydvrse.shared.domain.BaseEntity;
import com.rydvrse.shared.domain.GeoLocation;
import com.rydvrse.shared.enums.TripStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Trip entity — the CORE business entity of RYDVRSE.
 * Represents a booking where a professional driver operates the customer's vehicle.
 *
 * Trip lifecycle state machine:
 * REQUESTED → DRIVER_MATCHING → DRIVER_ASSIGNED → DRIVER_ARRIVING
 *           → TRIP_STARTED → TRIP_COMPLETED
 * Any pre-start state → CANCELLED
 */
@Entity
@Table(name = "trips", indexes = {
        @Index(name = "idx_trip_customer", columnList = "customer_id"),
        @Index(name = "idx_trip_driver", columnList = "driver_id"),
        @Index(name = "idx_trip_status", columnList = "status"),
        @Index(name = "idx_trip_created", columnList = "created_at")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Trip extends BaseEntity {

    @Column(name = "trip_number", unique = true, nullable = false, length = 20)
    private String tripNumber; // e.g., RYD-20260313-ABCD

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "driver_id")
    private UUID driverId;

    @Column(name = "vehicle_id")
    private UUID vehicleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private TripStatus status = TripStatus.REQUESTED;

    // Pickup location
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "pickup_lat")),
            @AttributeOverride(name = "longitude", column = @Column(name = "pickup_lng")),
            @AttributeOverride(name = "address", column = @Column(name = "pickup_address")),
            @AttributeOverride(name = "city", column = @Column(name = "pickup_city")),
            @AttributeOverride(name = "pincode", column = @Column(name = "pickup_pincode"))
    })
    private GeoLocation pickupLocation;

    // Drop location
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "drop_lat")),
            @AttributeOverride(name = "longitude", column = @Column(name = "drop_lng")),
            @AttributeOverride(name = "address", column = @Column(name = "drop_address")),
            @AttributeOverride(name = "city", column = @Column(name = "drop_city")),
            @AttributeOverride(name = "pincode", column = @Column(name = "drop_pincode"))
    })
    private GeoLocation dropLocation;

    // Distance & duration
    @Column(name = "estimated_distance_km")
    private Double estimatedDistanceKm;

    @Column(name = "actual_distance_km")
    private Double actualDistanceKm;

    @Column(name = "estimated_duration_min")
    private Integer estimatedDurationMin;

    @Column(name = "actual_duration_min")
    private Integer actualDurationMin;

    // Fare
    @Column(name = "estimated_fare")
    private Double estimatedFare;

    @Column(name = "final_fare")
    private Double finalFare;

    @Column(name = "surge_multiplier")
    @Builder.Default
    private Double surgeMultiplier = 1.0;

    // Timestamps
    @Column(name = "driver_assigned_at")
    private Instant driverAssignedAt;

    @Column(name = "driver_arrived_at")
    private Instant driverArrivedAt;

    @Column(name = "trip_started_at")
    private Instant tripStartedAt;

    @Column(name = "trip_completed_at")
    private Instant tripCompletedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Column(name = "cancelled_by")
    private String cancelledBy; // CUSTOMER, DRIVER, SYSTEM

    // Ratings
    @Column(name = "customer_rating")
    private Double customerRating;

    @Column(name = "driver_rating")
    private Double driverRating;

    @Column(name = "customer_feedback")
    private String customerFeedback;

    @Column(name = "driver_feedback")
    private String driverFeedback;

    // OTP for trip start verification
    @Column(name = "start_otp", length = 4)
    private String startOtp;

    /**
     * Generate a unique trip number.
     */
    @PrePersist
    public void generateTripNumber() {
        if (this.tripNumber == null) {
            String datePart = java.time.LocalDate.now().toString().replace("-", "");
            String randomPart = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
            this.tripNumber = "RYD-" + datePart + "-" + randomPart;
        }
    }
}
