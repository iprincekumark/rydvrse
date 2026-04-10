package com.rydvrse.customer.infrastructure;

import com.rydvrse.customer.domain.CustomerProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerProfileRepository extends JpaRepository<CustomerProfileEntity, UUID> {

    Optional<CustomerProfileEntity> findByUserAccountId(UUID userAccountId);
}
