package com.rydvrse.finance.infrastructure;

import com.rydvrse.finance.domain.InvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<InvoiceEntity, UUID> {

    Optional<InvoiceEntity> findByBookingId(UUID bookingId);
}
