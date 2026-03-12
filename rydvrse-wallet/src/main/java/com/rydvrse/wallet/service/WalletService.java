package com.rydvrse.wallet.service;

import com.rydvrse.payment.event.PaymentCompletedEvent;
import com.rydvrse.shared.exception.BusinessRuleException;
import com.rydvrse.shared.exception.ResourceNotFoundException;
import com.rydvrse.wallet.domain.Wallet;
import com.rydvrse.wallet.domain.WalletTransaction;
import com.rydvrse.wallet.repository.WalletRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Wallet service — manages balances for customers and drivers.
 * Listens to PaymentCompletedEvent to credit driver earnings.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final EntityManager entityManager;

    @EventListener
    @Transactional
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        // Credit the driver's wallet with their payout
        Wallet driverWallet = getOrCreateWallet(event.getDriverId(), "DRIVER");
        credit(driverWallet.getId(), event.getDriverPayout(), "CREDIT",
                "Earnings from trip payment " + event.getAggregateId(), event.getAggregateId());
        log.info("Credited ₹{} to driver {} wallet", event.getDriverPayout(), event.getDriverId());
    }

    @Transactional
    public Wallet getOrCreateWallet(UUID userId, String userType) {
        return walletRepository.findByUserIdAndUserType(userId, userType)
                .orElseGet(() -> walletRepository.save(Wallet.builder()
                        .userId(userId).userType(userType).build()));
    }

    @Transactional
    public void credit(UUID walletId, double amount, String txnType, String description, UUID refId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet", walletId.toString()));
        wallet.setBalance(wallet.getBalance() + amount);
        walletRepository.save(wallet);

        WalletTransaction txn = WalletTransaction.builder()
                .walletId(walletId).amount(amount).transactionType(txnType)
                .description(description).referenceId(refId).balanceAfter(wallet.getBalance())
                .build();
        entityManager.persist(txn);
    }

    @Transactional
    public void debit(UUID walletId, double amount, String description, UUID refId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet", walletId.toString()));
        if (wallet.getBalance() < amount) {
            throw new BusinessRuleException("Insufficient wallet balance");
        }
        wallet.setBalance(wallet.getBalance() - amount);
        walletRepository.save(wallet);

        WalletTransaction txn = WalletTransaction.builder()
                .walletId(walletId).amount(amount).transactionType("DEBIT")
                .description(description).referenceId(refId).balanceAfter(wallet.getBalance())
                .build();
        entityManager.persist(txn);
    }
}
