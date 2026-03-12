package com.rydvrse.payment.repository;

import com.rydvrse.payment.domain.Payment;
import com.rydvrse.shared.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByTripId(UUID tripId);
    Optional<Payment> findByTransactionId(String transactionId);
    Page<Payment> findByCustomerIdOrderByCreatedAtDesc(UUID customerId, Pageable pageable);
    Page<Payment> findByDriverIdAndStatusOrderByCreatedAtDesc(UUID driverId, PaymentStatus status, Pageable pageable);
}
