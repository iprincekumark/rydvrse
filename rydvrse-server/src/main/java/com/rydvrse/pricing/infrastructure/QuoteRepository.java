package com.rydvrse.pricing.infrastructure;

import com.rydvrse.pricing.domain.QuoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface QuoteRepository extends JpaRepository<QuoteEntity, UUID> {

    Optional<QuoteEntity> findByIdAndCustomerProfileId(UUID id, UUID customerProfileId);
}
