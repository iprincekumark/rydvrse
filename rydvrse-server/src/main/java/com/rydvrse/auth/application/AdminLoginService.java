package com.rydvrse.auth.application;

import com.rydvrse.auth.domain.RoleEntity;
import com.rydvrse.auth.domain.UserAccountEntity;
import com.rydvrse.auth.domain.UserRoleBindingEntity;
import com.rydvrse.auth.infrastructure.RoleRepository;
import com.rydvrse.auth.infrastructure.UserAccountRepository;
import com.rydvrse.auth.infrastructure.UserRoleBindingRepository;
import com.rydvrse.common.error.ApiException;
import com.rydvrse.common.error.ErrorCode;
import com.rydvrse.common.security.ActorType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class AdminLoginService {

    private final UserAccountRepository userAccountRepository;
    private final UserRoleBindingRepository userRoleBindingRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;

    public AdminLoginService(
            UserAccountRepository userAccountRepository,
            UserRoleBindingRepository userRoleBindingRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            SessionService sessionService
    ) {
        this.userAccountRepository = userAccountRepository;
        this.userRoleBindingRepository = userRoleBindingRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionService = sessionService;
    }

    @Transactional
    public AdminAuthenticatedActor login(String email, String password, String requestIp, String userAgent) {
        UserAccountEntity account = userAccountRepository.findByEmailIgnoreCase(email)
                .filter(found -> "ADMIN".equals(found.getUserType()))
                .orElseThrow(() -> ApiException.unauthorized(ErrorCode.UNAUTHORIZED, "Invalid admin credentials"));
        if (account.getPasswordHash() == null || !passwordEncoder.matches(password, account.getPasswordHash())) {
            throw ApiException.unauthorized(ErrorCode.UNAUTHORIZED, "Invalid admin credentials");
        }
        List<UserRoleBindingEntity> bindings = userRoleBindingRepository
                .findActiveBindings(account.getId(), "ACTIVE", OffsetDateTime.now());
        List<RoleEntity> roles = roleRepository.findByIdIn(bindings.stream().map(UserRoleBindingEntity::getRoleId).toList());
        SessionTokens tokens = sessionService.createSession(
                account,
                null,
                ActorType.ADMIN,
                roles.stream().map(RoleEntity::getRoleCode).toList(),
                requestIp,
                userAgent
        );
        return new AdminAuthenticatedActor(account, roles.stream().map(RoleEntity::getRoleCode).toList(), tokens);
    }

    public record AdminAuthenticatedActor(UserAccountEntity userAccount, List<String> roles, SessionTokens tokens) {
    }
}
