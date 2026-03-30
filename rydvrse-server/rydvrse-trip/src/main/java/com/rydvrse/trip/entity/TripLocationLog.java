package com.rydvrse.trip.entity;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "trip_location_log", indexes = {
        @Index(name = "idx_trip_loc_trip_time", columnList = "trip_id, timestamp")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TripLocationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "trip_id", nullable = false)
    private UUID tripId;

    @Column(name = "point", nullable = false, columnDefinition = "geometry(Point, 4326)")
    private Point point;

    @Column(name = "heading")
    private Double heading;

    @Column(name = "speed")
    private Double speed;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;
}
