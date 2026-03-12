package com.rydvrse.safety.domain;

import com.rydvrse.shared.domain.BaseEntity;
import com.rydvrse.shared.domain.GeoLocation;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "safety_incidents", indexes = {
        @Index(name = "idx_incident_trip", columnList = "trip_id"),
        @Index(name = "idx_incident_type", columnList = "incident_type"),
        @Index(name = "idx_incident_status", columnList = "status")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SafetyIncident extends BaseEntity {

    @Column(name = "trip_id")
    private UUID tripId;

    @Column(name = "reported_by")
    private UUID reportedBy;

    @Column(name = "reporter_type", length = 20)
    private String reporterType; // CUSTOMER, DRIVER, SYSTEM

    @Column(name = "incident_type", nullable = false, length = 50)
    private String incidentType; // SOS, ACCIDENT, HARASSMENT, RECKLESS_DRIVING, OTHER

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Embedded
    private GeoLocation location;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "REPORTED"; // REPORTED, INVESTIGATING, RESOLVED, ESCALATED

    @Column(name = "priority", length = 10)
    @Builder.Default
    private String priority = "HIGH";

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;
}
