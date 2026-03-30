package com.rydvrse.safety.entity;

import jakarta.persistence.*;
import lombok.*; import java.time.Instant; import java.util.UUID;

@Entity @Table(name = "trip_share_link")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TripShareLink {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "trip_id", nullable = false) private UUID tripId;
    @Column(name = "customer_id", nullable = false) private UUID customerId;
    @Column(name = "share_token", nullable = false, unique = true) @Builder.Default private UUID shareToken = UUID.randomUUID();
    @Column(name = "recipient_phone", nullable = false, length = 15) private String recipientPhone;
    @Column(name = "recipient_name", nullable = false, length = 100) private String recipientName;
    @Column(name = "is_active", nullable = false) @Builder.Default private Boolean isActive = true;
    @Column(name = "expires_at", nullable = false) private Instant expiresAt;
}
