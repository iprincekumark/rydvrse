package com.rydvrse.finance.infrastructure;

import com.rydvrse.finance.domain.RefundDecisionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefundDecisionRepository extends JpaRepository<RefundDecisionEntity, UUID> {

    Optional<RefundDecisionEntity> findByRefundRequestId(UUID refundRequestId);
}
