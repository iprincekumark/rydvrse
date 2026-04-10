package com.rydvrse.finance.infrastructure;

import com.rydvrse.finance.domain.PaymentOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrderEntity, UUID> {

    Optional<PaymentOrderEntity> findByBookingId(UUID bookingId);

    Optional<PaymentOrderEntity> findByProviderOrderId(String providerOrderId);
}
