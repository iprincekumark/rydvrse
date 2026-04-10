package com.rydvrse.finance.infrastructure;

import com.rydvrse.finance.domain.RefundRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RefundRequestRepository extends JpaRepository<RefundRequestEntity, UUID> {

    List<RefundRequestEntity> findByBookingIdOrderByRequestedAtDesc(UUID bookingId);

    List<RefundRequestEntity> findAllByOrderByRequestedAtDesc();
}
