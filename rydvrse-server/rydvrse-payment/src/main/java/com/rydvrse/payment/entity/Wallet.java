package com.rydvrse.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "wallet")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Wallet {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "customer_id", nullable = false, unique = true) private UUID customerId;
    @Column(name = "balance", nullable = false, precision = 10, scale = 2) @Builder.Default private BigDecimal balance = BigDecimal.ZERO;
    @Column(name = "currency", nullable = false, length = 3) @Builder.Default private String currency = "INR";
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @PrePersist @PreUpdate void prePersistUpdate() { updatedAt = Instant.now(); }
}
