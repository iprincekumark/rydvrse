package com.rydvrse.finance.infrastructure;

import com.rydvrse.finance.domain.RefundTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RefundTransactionRepository extends JpaRepository<RefundTransactionEntity, UUID> {

    List<RefundTransactionEntity> findByRefundRequestIdOrderByCreatedAtAsc(UUID refundRequestId);
}
