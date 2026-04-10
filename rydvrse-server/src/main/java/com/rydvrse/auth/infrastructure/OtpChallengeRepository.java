package com.rydvrse.auth.infrastructure;

import com.rydvrse.auth.domain.OtpChallengeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OtpChallengeRepository extends JpaRepository<OtpChallengeEntity, UUID> {

    Optional<OtpChallengeEntity> findTopByMobileNumberE164OrderByCreatedAtDesc(String mobileNumberE164);
}
