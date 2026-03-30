package com.rydvrse.admin.entity;

import com.rydvrse.shared.enums.AdminRole;
import com.rydvrse.shared.enums.AdminStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "admin_user")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminUser {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "name", nullable = false, length = 100) private String name;
    @Column(name = "email", nullable = false, unique = true) private String email;
    @Column(name = "password_hash", nullable = false) private String passwordHash;
    @Enumerated(EnumType.STRING) @Column(name = "role", nullable = false) private AdminRole role;
    @Enumerated(EnumType.STRING) @Column(name = "status", nullable = false) @Builder.Default private AdminStatus status = AdminStatus.ACTIVE;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @PrePersist void prePersist() { if (createdAt == null) createdAt = Instant.now(); }
}
