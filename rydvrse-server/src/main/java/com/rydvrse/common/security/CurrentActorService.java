package com.rydvrse.common.security;

import com.rydvrse.common.error.ApiException;
import com.rydvrse.common.error.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CurrentActorService {

    public Optional<RydvrsePrincipal> getCurrentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof RydvrsePrincipal principal)) {
            return Optional.empty();
        }
        return Optional.of(principal);
    }

    public RydvrsePrincipal requireCurrentActor() {
        return getCurrentActor()
                .orElseThrow(() -> ApiException.unauthorized(ErrorCode.UNAUTHORIZED, "Authentication required"));
    }

    public RydvrsePrincipal requireActor(ActorType actorType) {
        RydvrsePrincipal principal = requireCurrentActor();
        if (principal.actorType() != actorType) {
            throw ApiException.forbidden(ErrorCode.FORBIDDEN, "Actor type not allowed for this endpoint");
        }
        return principal;
    }

    public boolean hasRole(String role) {
        return requireCurrentActor().roles().contains(role);
    }
}
