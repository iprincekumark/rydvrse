package com.rydvrse.payment.entity;

import com.rydvrse.shared.enums.WalletReferenceType;
import com.rydvrse.shared.enums.WalletTransactionType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "wallet_transaction")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WalletTransaction {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "wallet_id", nullable = false) private UUID walletId;
    @Enumerated(EnumType.STRING) @Column(name = "type", nullable = false) private WalletTransactionType type;
    @Column(name = "amount", nullable = false, precision = 10, scale = 2) private BigDecimal amount;
    @Enumerated(EnumType.STRING) @Column(name = "reference_type", nullable = false) private WalletReferenceType referenceType;
    @Column(name = "reference_id", nullable = false) private UUID referenceId;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @PrePersist void prePersist() { if (createdAt == null) createdAt = Instant.now(); }
}
