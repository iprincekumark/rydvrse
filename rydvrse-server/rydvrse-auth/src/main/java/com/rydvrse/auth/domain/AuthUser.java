package com.rydvrse.auth.domain;

import com.rydvrse.shared.domain.BaseEntity;
import com.rydvrse.shared.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Core authentication entity. Stores credentials & role for ALL user types.
 * This is the Auth module's own entity — separate from User/Driver profile data.
 * Each Customer, Driver, or Admin has exactly one AuthUser record.
 */
@Entity
@Table(name = "auth_users", indexes = {
        @Index(name = "idx_auth_phone", columnList = "phone_number", unique = true),
        @Index(name = "idx_auth_email", columnList = "email"),
        @Index(name = "idx_auth_role", columnList = "role")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthUser extends BaseEntity {

    @Column(name = "phone_number", nullable = false, unique = true, length = 15)
    private String phoneNumber;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    /** Links to the Customer/Driver/Admin profile UUID in their respective modules */
    @Column(name = "profile_id")
    private UUID profileId;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_phone_verified", nullable = false)
    @Builder.Default
    private Boolean isPhoneVerified = false;

    @Column(name = "is_email_verified", nullable = false)
    @Builder.Default
    private Boolean isEmailVerified = false;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "failed_login_attempts")
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private Instant lockedUntil;
}
