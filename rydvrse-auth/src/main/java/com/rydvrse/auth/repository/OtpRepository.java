package com.rydvrse.auth.repository;

import com.rydvrse.auth.domain.OtpRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpRepository extends JpaRepository<OtpRecord, UUID> {
    Optional<OtpRecord> findTopByPhoneNumberAndIsUsedFalseOrderByCreatedAtDesc(String phoneNumber);
}
