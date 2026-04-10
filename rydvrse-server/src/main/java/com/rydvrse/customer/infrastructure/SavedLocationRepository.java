package com.rydvrse.customer.infrastructure;

import com.rydvrse.customer.domain.SavedLocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SavedLocationRepository extends JpaRepository<SavedLocationEntity, UUID> {

    List<SavedLocationEntity> findByCustomerProfileIdAndDeletedFalseOrderByDefaultLocationDescCreatedAtAsc(UUID customerProfileId);

    Optional<SavedLocationEntity> findByIdAndCustomerProfileIdAndDeletedFalse(UUID id, UUID customerProfileId);
}
