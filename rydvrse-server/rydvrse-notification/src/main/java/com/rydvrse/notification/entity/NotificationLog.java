package com.rydvrse.notification.entity;

import com.rydvrse.shared.enums.NotificationChannel;
import com.rydvrse.shared.enums.NotificationStatus;
import com.rydvrse.shared.enums.UserType;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "notification_log")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationLog {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Enumerated(EnumType.STRING) @Column(name = "recipient_type", nullable = false) private UserType recipientType;
    @Column(name = "recipient_id", nullable = false) private UUID recipientId;
    @Enumerated(EnumType.STRING) @Column(name = "channel", nullable = false) private NotificationChannel channel;
    @Column(name = "type", nullable = false, length = 50) private String type;
    @Column(name = "title", nullable = false) private String title;
    @Column(name = "body", nullable = false, columnDefinition = "text") private String body;
    @Enumerated(EnumType.STRING) @Column(name = "status", nullable = false) @Builder.Default private NotificationStatus status = NotificationStatus.SENT;
    @Column(name = "sent_at", nullable = false) private Instant sentAt;
    @PrePersist void prePersist() { if (sentAt == null) sentAt = Instant.now(); }
}
