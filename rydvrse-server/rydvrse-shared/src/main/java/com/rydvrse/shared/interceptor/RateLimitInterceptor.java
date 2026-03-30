package com.rydvrse.shared.interceptor;

import com.rydvrse.shared.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;

/**
 * Redis sliding-window rate limiter.
 * Configurable per-endpoint limits per spec §7.4.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate redisTemplate;

    private static final int DEFAULT_LIMIT = 100;
    private static final Duration DEFAULT_WINDOW = Duration.ofMinutes(1);

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        String userId = request.getHeader("X-User-Id");
        if (userId == null) {
            userId = request.getRemoteAddr();
        }

        String path = request.getRequestURI();
        int limit = getLimit(path);
        Duration window = getWindow(path);

        String key = "rate_limit:" + path + ":" + userId;

        Long currentCount = redisTemplate.opsForValue().increment(key);
        if (currentCount != null && currentCount == 1) {
            redisTemplate.expire(key, window);
        }

        if (currentCount != null && currentCount > limit) {
            throw new RateLimitExceededException(path);
        }

        return true;
    }

    private int getLimit(String path) {
        if (path.contains("/auth/otp/send")) return 5;
        if (path.contains("/auth/otp/verify")) return 3;
        if (path.contains("/auth/login")) return 10;
        if (path.contains("/trips/book")) return 5;
        if (path.contains("/drivers/me/location")) return 60;
        return DEFAULT_LIMIT;
    }

    private Duration getWindow(String path) {
        if (path.contains("/auth/otp/send")) return Duration.ofHours(1);
        if (path.contains("/auth/login")) return Duration.ofMinutes(15);
        return DEFAULT_WINDOW;
    }
}
