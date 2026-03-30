package com.rydvrse.driver.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "driver_availability", indexes = {
        @Index(name = "idx_driver_avail_driver", columnList = "driver_id", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DriverAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "driver_id", nullable = false, unique = true)
    private UUID driverId;

    @Column(name = "is_online", nullable = false)
    @Builder.Default
    private Boolean isOnline = false;

    @Column(name = "current_trip_id")
    private UUID currentTripId;

    @Column(name = "last_online_at")
    private Instant lastOnlineAt;
}
