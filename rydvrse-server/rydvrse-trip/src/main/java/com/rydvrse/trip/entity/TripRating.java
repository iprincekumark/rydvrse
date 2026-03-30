package com.rydvrse.trip.entity;

import com.rydvrse.shared.enums.RatedBy;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "trip_rating")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TripRating {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "trip_id", nullable = false) private UUID tripId;
    @Enumerated(EnumType.STRING) @Column(name = "rated_by", nullable = false) private RatedBy ratedBy;
    @Column(name = "rater_id", nullable = false) private UUID raterId;
    @Column(name = "rating", nullable = false) private Integer rating;
    @Column(name = "review", columnDefinition = "text") private String review;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @PrePersist void prePersist() { if (createdAt == null) createdAt = Instant.now(); }
}
