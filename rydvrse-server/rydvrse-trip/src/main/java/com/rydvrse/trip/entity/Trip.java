package com.rydvrse.trip.entity;

import com.rydvrse.shared.domain.BaseEntity;
import com.rydvrse.shared.enums.TripCancelledBy;
import com.rydvrse.shared.enums.TripStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "trip", indexes = {
        @Index(name = "idx_trip_customer_status", columnList = "customer_id, status"),
        @Index(name = "idx_trip_driver_status", columnList = "driver_id, status"),
        @Index(name = "idx_trip_scheduled", columnList = "scheduled_at")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Trip extends BaseEntity {

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "driver_id")
    private UUID driverId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private TripStatus status = TripStatus.PENDING;

    @Column(name = "pickup_address", nullable = false, length = 500)
    private String pickupAddress;

    @Column(name = "pickup_lat", nullable = false)
    private Double pickupLat;

    @Column(name = "pickup_lng", nullable = false)
    private Double pickupLng;

    @Column(name = "drop_address", nullable = false, length = 500)
    private String dropAddress;

    @Column(name = "drop_lat", nullable = false)
    private Double dropLat;

    @Column(name = "drop_lng", nullable = false)
    private Double dropLng;

    @Column(name = "estimated_fare", nullable = false, precision = 10, scale = 2)
    private BigDecimal estimatedFare;

    @Column(name = "actual_fare", precision = 10, scale = 2)
    private BigDecimal actualFare;

    @Column(name = "estimated_distance", nullable = false, precision = 10, scale = 2)
    private BigDecimal estimatedDistance;

    @Column(name = "actual_distance", precision = 10, scale = 2)
    private BigDecimal actualDistance;

    @Column(name = "estimated_duration", nullable = false)
    private Integer estimatedDuration;

    @Column(name = "scheduled_at")
    private Instant scheduledAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "cancelled_by")
    private TripCancelledBy cancelledBy;

    @Column(name = "is_night_trip", nullable = false)
    @Builder.Default
    private Boolean isNightTrip = false;
}
