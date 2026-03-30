package com.rydvrse.safety.entity;

import com.rydvrse.shared.enums.UserType;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "emergency_contact")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmergencyContact {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Enumerated(EnumType.STRING) @Column(name = "user_type", nullable = false) private UserType userType;
    @Column(name = "name", nullable = false, length = 100) private String name;
    @Column(name = "phone", nullable = false, length = 15) private String phone;
    @Column(name = "relationship", nullable = false, length = 50) private String relationship;
    @Column(name = "is_primary", nullable = false) @Builder.Default private Boolean isPrimary = false;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @PrePersist void prePersist() { if (createdAt == null) createdAt = Instant.now(); }
}
