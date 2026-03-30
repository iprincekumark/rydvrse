package com.rydvrse.payment.service;

import com.rydvrse.payment.entity.Payment;
import com.rydvrse.payment.entity.Wallet;
import com.rydvrse.payment.repository.PaymentRepository;
import com.rydvrse.payment.repository.WalletRepository;
import com.rydvrse.shared.enums.PaymentMethod;
import com.rydvrse.shared.enums.PaymentStatus;
import com.rydvrse.shared.exception.ResourceNotFoundException;
import com.rydvrse.shared.service.IdempotencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j @Service @RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final WalletRepository walletRepository;
    private final IdempotencyService idempotencyService;

    @Transactional
    public Payment initiatePayment(UUID tripId, UUID customerId, BigDecimal amount,
                                   PaymentMethod method, String idempotencyKey) {
        if (idempotencyService.isDuplicate(idempotencyKey)) {
            return paymentRepository.findByIdempotencyKey(idempotencyKey).orElseThrow();
        }
        Payment payment = Payment.builder()
                .tripId(tripId).customerId(customerId).amount(amount)
                .method(method).idempotencyKey(idempotencyKey)
                .status(PaymentStatus.PROCESSING).build();
        payment = paymentRepository.save(payment);
        // Simulate gateway processing — in production, call Razorpay API
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setGatewayTransactionId("rzp_" + UUID.randomUUID().toString().substring(0, 14));
        payment = paymentRepository.save(payment);
        idempotencyService.markProcessed(idempotencyKey, payment.getId().toString());
        log.info("Payment {} processed for trip {}", payment.getId(), tripId);
        return payment;
    }

    @Transactional(readOnly = true)
    public Payment getPayment(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId.toString()));
    }

    @Transactional(readOnly = true)
    public Wallet getWallet(UUID customerId) {
        return walletRepository.findByCustomerId(customerId)
                .orElseGet(() -> walletRepository.save(
                        Wallet.builder().customerId(customerId).build()));
    }

    @Transactional
    public Payment refund(UUID paymentId) {
        Payment payment = getPayment(paymentId);
        payment.setStatus(PaymentStatus.REFUNDED);
        return paymentRepository.save(payment);
    }
}
