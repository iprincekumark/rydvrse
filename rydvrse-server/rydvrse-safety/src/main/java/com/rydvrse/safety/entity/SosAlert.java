package com.rydvrse.safety.entity;

import com.rydvrse.shared.enums.SosStatus;
import com.rydvrse.shared.enums.UserType;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "sos_alert", indexes = {@Index(name = "idx_sos_status", columnList = "status")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SosAlert {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "trip_id", nullable = false) private UUID tripId;
    @Enumerated(EnumType.STRING) @Column(name = "triggered_by", nullable = false) private UserType triggeredBy;
    @Column(name = "triggered_by_id", nullable = false) private UUID triggeredById;
    @Column(name = "lat", nullable = false) private Double lat;
    @Column(name = "lng", nullable = false) private Double lng;
    @Enumerated(EnumType.STRING) @Column(name = "status", nullable = false) @Builder.Default private SosStatus status = SosStatus.ACTIVE;
    @Column(name = "resolved_at") private Instant resolvedAt;
    @Column(name = "resolved_by") private UUID resolvedBy;
    @Column(name = "notes", columnDefinition = "text") private String notes;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @PrePersist void prePersist() { if (createdAt == null) createdAt = Instant.now(); }
}
