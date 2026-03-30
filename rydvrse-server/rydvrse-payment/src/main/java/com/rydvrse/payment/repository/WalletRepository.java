package com.rydvrse.payment.repository;

import com.rydvrse.payment.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    Optional<Wallet> findByCustomerId(UUID customerId);
}
