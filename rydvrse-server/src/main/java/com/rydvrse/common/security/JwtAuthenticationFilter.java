package com.rydvrse.common.security;

import com.rydvrse.common.error.ApiException;
import com.rydvrse.common.error.ErrorCode;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final List<String> PUBLIC_PATTERNS = List.of(
            "/api/v1/auth/**",
            "/api/v1/admin/auth/**",
            "/api/v1/config/**",
            "/api/v1/webhooks/**",
            "/api/docs/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/actuator/health/**"
    );

    private final JwtService jwtService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return PUBLIC_PATTERNS.stream().anyMatch(pattern -> pathMatcher.match(pattern, request.getServletPath()));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            throw ApiException.unauthorized(ErrorCode.UNAUTHORIZED, "Missing bearer token");
        }

        Claims claims = jwtService.parse(header.substring(7));
        List<String> roles = claims.get("roles", List.class);
        RydvrsePrincipal principal = new RydvrsePrincipal(
                UUID.fromString(claims.getSubject()),
                claims.get("profile_id") == null ? null : UUID.fromString(claims.get("profile_id", String.class)),
                UUID.fromString(claims.get("session_id", String.class)),
                ActorType.valueOf(claims.get("actor_type", String.class)),
                roles == null ? List.of() : roles
        );
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.roles().stream().map(SimpleGrantedAuthority::new).toList()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }
}
