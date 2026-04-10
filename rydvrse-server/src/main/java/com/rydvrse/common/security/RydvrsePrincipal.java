package com.rydvrse.common.security;

import java.util.List;
import java.util.UUID;

public record RydvrsePrincipal(
        UUID userId,
        UUID profileId,
        UUID sessionId,
        ActorType actorType,
        List<String> roles
) {
}
