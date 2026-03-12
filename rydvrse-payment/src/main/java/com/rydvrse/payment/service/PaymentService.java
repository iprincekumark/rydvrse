package com.rydvrse.payment.service;

import com.rydvrse.payment.domain.Payment;
import com.rydvrse.payment.event.PaymentCompletedEvent;
import com.rydvrse.payment.repository.PaymentRepository;
import com.rydvrse.shared.enums.PaymentMethod;
import com.rydvrse.shared.enums.PaymentStatus;
import com.rydvrse.shared.event.EventPublisher;
import com.rydvrse.shared.exception.ResourceNotFoundException;
import com.rydvrse.trip.event.TripCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Payment service — handles entire financial lifecycle.
 *
 * Listens to TripCompletedEvent → creates payment → processes payment.
 * Publishes PaymentCompletedEvent → consumed by Notification & Wallet.
 *
 * Platform takes 20% commission; 80% goes to driver payout.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final EventPublisher eventPublisher;

    private static final double PLATFORM_COMMISSION_PCT = 20.0;

    /**
     * Auto-create payment when trip completes.
     */
    @EventListener
    @Transactional
    public void onTripCompleted(TripCompletedEvent event) {
        log.info("Creating payment for trip {}: ₹{}", event.getAggregateId(), event.getFinalFare());

        double platformFee = event.getFinalFare() * (PLATFORM_COMMISSION_PCT / 100);
        double driverPayout = event.getFinalFare() - platformFee;

        Payment payment = Payment.builder()
                .tripId(event.getAggregateId())
                .customerId(event.getCustomerId())
                .driverId(event.getDriverId())
                .amount(event.getFinalFare())
                .platformFee(Math.round(platformFee * 100.0) / 100.0)
                .driverPayout(Math.round(driverPayout * 100.0) / 100.0)
                .paymentMethod(PaymentMethod.UPI) // Default; in production, use customer's preferred method
                .status(PaymentStatus.PENDING)
                .build();

        paymentRepository.save(payment);
        processPayment(payment.getId());
    }

    /**
     * Process payment (simulate payment gateway interaction).
     */
    @Transactional
    public Payment processPayment(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId.toString()));

        // In production: call payment gateway (Razorpay, Stripe, etc.)
        // Simulate successful payment
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setPaidAt(Instant.now());
        payment.setGatewayReference("GW-" + UUID.randomUUID().toString().substring(0, 8));
        Payment saved = paymentRepository.save(payment);

        // Publish event → Notification (receipt), Analytics (revenue tracking)
        eventPublisher.publish(new PaymentCompletedEvent(
                saved.getId(), saved.getTripId(), saved.getCustomerId(),
                saved.getDriverId(), saved.getAmount(), saved.getDriverPayout()));
        eventPublisher.publishAsync(new PaymentCompletedEvent(
                saved.getId(), saved.getTripId(), saved.getCustomerId(),
                saved.getDriverId(), saved.getAmount(), saved.getDriverPayout()));

        log.info("Payment {} completed: ₹{} (driver payout: ₹{})",
                saved.getTransactionId(), saved.getAmount(), saved.getDriverPayout());
        return saved;
    }

    /**
     * Process refund.
     */
    @Transactional
    public Payment refundPayment(UUID paymentId, Double refundAmount, String reason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId.toString()));

        double amount = refundAmount != null ? refundAmount : payment.getAmount();
        payment.setRefundAmount(amount);
        payment.setRefundedAt(Instant.now());
        payment.setStatus(Double.compare(amount, payment.getAmount()) == 0
                ? PaymentStatus.REFUNDED : PaymentStatus.PARTIALLY_REFUNDED);

        log.info("Refund processed for payment {}: ₹{}", payment.getTransactionId(), amount);
        return paymentRepository.save(payment);
    }
}
