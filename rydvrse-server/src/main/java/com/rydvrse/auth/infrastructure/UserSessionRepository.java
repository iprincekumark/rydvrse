package com.rydvrse.auth.infrastructure;

import com.rydvrse.auth.domain.UserSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserSessionRepository extends JpaRepository<UserSessionEntity, UUID> {

    Optional<UserSessionEntity> findByRefreshTokenHash(String refreshTokenHash);

    Optional<UserSessionEntity> findByIdAndUserAccountId(UUID id, UUID userAccountId);
}
