package com.rydvrse.notification.entity;

import com.rydvrse.shared.enums.DevicePlatform;
import com.rydvrse.shared.enums.UserType;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "device_token")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DeviceToken {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Enumerated(EnumType.STRING) @Column(name = "user_type", nullable = false) private UserType userType;
    @Enumerated(EnumType.STRING) @Column(name = "platform", nullable = false) private DevicePlatform platform;
    @Column(name = "fcm_token", nullable = false, length = 500) private String fcmToken;
    @Column(name = "is_active", nullable = false) @Builder.Default private Boolean isActive = true;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @PrePersist @PreUpdate void prePersistUpdate() { updatedAt = Instant.now(); }
}
