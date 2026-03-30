package com.rydvrse.driver.entity;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "driver_location", indexes = {
        @Index(name = "idx_driver_location_driver", columnList = "driver_id", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DriverLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "driver_id", nullable = false, unique = true)
    private UUID driverId;

    @Column(name = "point", nullable = false, columnDefinition = "geometry(Point, 4326)")
    private Point point;

    @Column(name = "heading")
    private Double heading;

    @Column(name = "speed")
    private Double speed;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    void prePersistUpdate() {
        updatedAt = Instant.now();
    }
}
