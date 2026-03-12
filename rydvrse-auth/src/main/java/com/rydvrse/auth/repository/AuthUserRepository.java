package com.rydvrse.auth.repository;

import com.rydvrse.auth.domain.AuthUser;
import com.rydvrse.shared.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthUserRepository extends JpaRepository<AuthUser, UUID> {
    Optional<AuthUser> findByPhoneNumber(String phoneNumber);
    Optional<AuthUser> findByEmail(String email);
    Optional<AuthUser> findByProfileIdAndRole(UUID profileId, UserRole role);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByEmail(String email);
}
