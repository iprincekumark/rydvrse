package com.rydvrse.safety.entity;

import com.rydvrse.shared.enums.IncidentPriority;
import com.rydvrse.shared.enums.IncidentStatus;
import com.rydvrse.shared.enums.IncidentType;
import com.rydvrse.shared.enums.UserType;
import com.rydvrse.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity @Table(name = "incident")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Incident extends BaseEntity {
    @Column(name = "trip_id") private UUID tripId;
    @Enumerated(EnumType.STRING) @Column(name = "reported_by", nullable = false) private UserType reportedBy;
    @Column(name = "reporter_id", nullable = false) private UUID reporterId;
    @Enumerated(EnumType.STRING) @Column(name = "type", nullable = false) private IncidentType type;
    @Column(name = "description", nullable = false, columnDefinition = "text") private String description;
    @Enumerated(EnumType.STRING) @Column(name = "status", nullable = false) @Builder.Default private IncidentStatus status = IncidentStatus.OPEN;
    @Enumerated(EnumType.STRING) @Column(name = "priority", nullable = false) private IncidentPriority priority;
}
