package com.rydvrse.finance.infrastructure;

import com.rydvrse.finance.domain.PaymentTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransactionEntity, UUID> {

    List<PaymentTransactionEntity> findByPaymentOrderIdOrderByRecordedAtAsc(UUID paymentOrderId);

    Optional<PaymentTransactionEntity> findTopByPaymentOrderIdOrderByRecordedAtDesc(UUID paymentOrderId);

    Optional<PaymentTransactionEntity> findByProviderTransactionId(String providerTransactionId);
}
