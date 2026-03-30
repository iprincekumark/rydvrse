package com.rydvrse.shared.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

/**
 * Represents the authenticated user in the security context.
 * Available via SecurityContextHolder after JWT validation.
 */
@Getter
@AllArgsConstructor
public class UserPrincipal {
    private final UUID userId;
    private final String userType;
    private final String role;
}
