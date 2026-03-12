package com.rydvrse.wallet.domain;

import com.rydvrse.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "wallet_transactions", indexes = {
        @Index(name = "idx_wt_wallet", columnList = "wallet_id"),
        @Index(name = "idx_wt_type", columnList = "transaction_type")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WalletTransaction extends BaseEntity {

    @Column(name = "wallet_id", nullable = false)
    private UUID walletId;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "transaction_type", nullable = false, length = 20)
    private String transactionType; // CREDIT, DEBIT, PROMO_CREDIT, REFUND

    @Column(name = "description")
    private String description;

    @Column(name = "reference_id")
    private UUID referenceId; // Payment ID, Promo ID, etc.

    @Column(name = "balance_after", nullable = false)
    private Double balanceAfter;
}
