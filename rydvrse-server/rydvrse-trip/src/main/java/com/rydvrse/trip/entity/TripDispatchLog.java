package com.rydvrse.trip.entity;

import com.rydvrse.shared.enums.DispatchStatus;
import com.rydvrse.shared.enums.DispatchType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "trip_dispatch_log")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TripDispatchLog {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "trip_id", nullable = false) private UUID tripId;
    @Column(name = "driver_id", nullable = false) private UUID driverId;
    @Enumerated(EnumType.STRING) @Column(name = "dispatch_type", nullable = false) private DispatchType dispatchType;
    @Enumerated(EnumType.STRING) @Column(name = "status", nullable = false) private DispatchStatus status;
    @Column(name = "sent_at", nullable = false) private Instant sentAt;
    @Column(name = "responded_at") private Instant respondedAt;
}
