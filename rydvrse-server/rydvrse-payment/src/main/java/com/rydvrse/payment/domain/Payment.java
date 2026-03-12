package com.rydvrse.payment.domain;

import com.rydvrse.shared.domain.BaseEntity;
import com.rydvrse.shared.enums.PaymentMethod;
import com.rydvrse.shared.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Payment entity — tracks the financial lifecycle of a trip payment.
 *
 * Payment flow:
 * 1. Trip completes → Payment created with status PENDING
 * 2. Payment authorized → AUTHORIZED (funds reserved)
 * 3. Payment captured → CAPTURED (funds deducted)
 * 4. Settlement → COMPLETED (driver payout scheduled)
 *
 * Supports: UPI, Credit Card, Debit Card, Wallet, Cash
 */
@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payment_trip", columnList = "trip_id"),
        @Index(name = "idx_payment_customer", columnList = "customer_id"),
        @Index(name = "idx_payment_status", columnList = "status"),
        @Index(name = "idx_payment_txn", columnList = "transaction_id", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment extends BaseEntity {

    @Column(name = "trip_id", nullable = false)
    private UUID tripId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "driver_id", nullable = false)
    private UUID driverId;

    @Column(name = "transaction_id", unique = true, length = 50)
    private String transactionId;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "platform_fee")
    @Builder.Default
    private Double platformFee = 0.0;

    @Column(name = "driver_payout")
    private Double driverPayout;

    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "gateway_reference")
    private String gatewayReference; // External payment gateway ref

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "refund_amount")
    private Double refundAmount;

    @Column(name = "refunded_at")
    private Instant refundedAt;

    @PrePersist
    public void generateTransactionId() {
        if (this.transactionId == null) {
            this.transactionId = "PAY-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        }
    }
}
