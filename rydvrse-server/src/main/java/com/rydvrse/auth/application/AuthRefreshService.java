package com.rydvrse.auth.application;

import com.rydvrse.auth.domain.RoleEntity;
import com.rydvrse.auth.domain.UserAccountEntity;
import com.rydvrse.auth.domain.UserSessionEntity;
import com.rydvrse.auth.domain.UserRoleBindingEntity;
import com.rydvrse.auth.infrastructure.RoleRepository;
import com.rydvrse.auth.infrastructure.UserAccountRepository;
import com.rydvrse.auth.infrastructure.UserSessionRepository;
import com.rydvrse.auth.infrastructure.UserRoleBindingRepository;
import com.rydvrse.common.error.ApiException;
import com.rydvrse.common.error.ErrorCode;
import com.rydvrse.common.security.ActorType;
import com.rydvrse.customer.domain.CustomerProfileEntity;
import com.rydvrse.customer.infrastructure.CustomerProfileRepository;
import com.rydvrse.driver.domain.DriverProfileEntity;
import com.rydvrse.driver.infrastructure.DriverProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AuthRefreshService {

    private final UserAccountRepository userAccountRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final UserSessionRepository userSessionRepository;
    private final UserRoleBindingRepository userRoleBindingRepository;
    private final RoleRepository roleRepository;
    private final SessionService sessionService;

    public AuthRefreshService(
            UserAccountRepository userAccountRepository,
            CustomerProfileRepository customerProfileRepository,
            DriverProfileRepository driverProfileRepository,
            UserSessionRepository userSessionRepository,
            UserRoleBindingRepository userRoleBindingRepository,
            RoleRepository roleRepository,
            SessionService sessionService
    ) {
        this.userAccountRepository = userAccountRepository;
        this.customerProfileRepository = customerProfileRepository;
        this.driverProfileRepository = driverProfileRepository;
        this.userSessionRepository = userSessionRepository;
        this.userRoleBindingRepository = userRoleBindingRepository;
        this.roleRepository = roleRepository;
        this.sessionService = sessionService;
    }

    @Transactional
    public RefreshedSession refresh(String refreshToken, String actorType) {
        UserSessionEntity session = userSessionRepository.findByRefreshTokenHash(com.rydvrse.common.util.HashingUtils.sha256(refreshToken))
                .orElseThrow(() -> ApiException.unauthorized(ErrorCode.SESSION_EXPIRED, "Refresh session not found"));
        UserAccountEntity account = userAccountRepository.findById(session.getUserAccountId())
                .orElseThrow(() -> ApiException.unauthorized(ErrorCode.SESSION_EXPIRED, "Account not found"));
        if (!account.getUserType().equals(actorType)) {
            throw ApiException.unauthorized(ErrorCode.SESSION_EXPIRED, "Actor type mismatch");
        }
        ActorType resolvedActorType = ActorType.valueOf(actorType);
        UUID profileId = null;
        List<String> roles = List.of();
        if (resolvedActorType == ActorType.CUSTOMER) {
            CustomerProfileEntity profile = customerProfileRepository.findByUserAccountId(account.getId())
                    .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "Customer profile not found"));
            profileId = profile.getId();
        } else if (resolvedActorType == ActorType.DRIVER) {
            DriverProfileEntity profile = driverProfileRepository.findByUserAccountId(account.getId())
                    .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "Driver profile not found"));
            profileId = profile.getId();
        } else {
            List<UserRoleBindingEntity> bindings = userRoleBindingRepository
                    .findActiveBindings(account.getId(), "ACTIVE", OffsetDateTime.now());
            roles = roleRepository.findByIdIn(bindings.stream().map(UserRoleBindingEntity::getRoleId).toList())
                    .stream().map(RoleEntity::getRoleCode).toList();
        }
        SessionTokens tokens = sessionService.refresh(refreshToken, profileId, resolvedActorType, account, roles);
        return new RefreshedSession(account, profileId, roles, tokens, resolvedActorType);
    }

    public record RefreshedSession(
            UserAccountEntity userAccount,
            UUID profileId,
            List<String> roles,
            SessionTokens sessionTokens,
            ActorType actorType
    ) {
    }
}
