package com.rydvrse.auth.infrastructure;

import com.rydvrse.auth.domain.UserAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserAccountRepository extends JpaRepository<UserAccountEntity, UUID> {

    Optional<UserAccountEntity> findByMobileNumberE164(String mobileNumberE164);

    Optional<UserAccountEntity> findByEmailIgnoreCase(String email);
}
