package com.rydvrse.payment.entity;

import com.rydvrse.shared.domain.BaseEntity;
import com.rydvrse.shared.enums.PaymentMethod;
import com.rydvrse.shared.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "payment", indexes = {
        @Index(name = "idx_payment_idempotency", columnList = "idempotency_key", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment extends BaseEntity {
    @Column(name = "trip_id", nullable = false) private UUID tripId;
    @Column(name = "customer_id", nullable = false) private UUID customerId;
    @Column(name = "amount", nullable = false, precision = 10, scale = 2) private BigDecimal amount;
    @Column(name = "currency", nullable = false, length = 3) @Builder.Default private String currency = "INR";
    @Enumerated(EnumType.STRING) @Column(name = "method", nullable = false) private PaymentMethod method;
    @Column(name = "gateway_provider", nullable = false, length = 50) @Builder.Default private String gatewayProvider = "RAZORPAY";
    @Column(name = "gateway_transaction_id") private String gatewayTransactionId;
    @Enumerated(EnumType.STRING) @Column(name = "status", nullable = false) @Builder.Default private PaymentStatus status = PaymentStatus.INITIATED;
    @Column(name = "idempotency_key", nullable = false, unique = true) private String idempotencyKey;
}
