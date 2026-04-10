package com.rydvrse.pricing.infrastructure;

import com.rydvrse.pricing.domain.QuoteComponentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuoteComponentRepository extends JpaRepository<QuoteComponentEntity, UUID> {

    List<QuoteComponentEntity> findByQuoteIdOrderBySortOrderAsc(UUID quoteId);
}
