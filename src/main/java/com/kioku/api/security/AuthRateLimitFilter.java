package com.kioku.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Caps how often one client may call the authentication endpoints.
 *
 * <p>Account lockout already blunts brute force against a single account, but
 * it does nothing about a caller working through many addresses, or hammering
 * registration. This limits by source address instead, which covers both.
 *
 * <p><strong>Single instance only.</strong> Counters live in memory, so each
 * replica enforces its own budget and the effective limit multiplies by the
 * number of instances. Moving this to a shared store is required before
 * running more than one.
 */
@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(AuthRateLimitFilter.class);
    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final int maxRequestsPerWindow;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public AuthRateLimitFilter(@Value("${auth.rate-limit.per-minute:20}") int maxRequestsPerWindow) {
        this.maxRequestsPerWindow = maxRequestsPerWindow;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/auth/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String client = clientKey(request);

        if (exceeded(client)) {
            logger.warn("Rate limit exceeded for {} on {}", client, request.getRequestURI());
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"message\":\"Too many requests. Try again shortly.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean exceeded(String client) {
        Instant now = Instant.now();
        Window window = windows.compute(client, (key, existing) ->
                existing == null || existing.startedBefore(now.minus(WINDOW))
                        ? new Window(now)
                        : existing);
        return window.count.incrementAndGet() > maxRequestsPerWindow;
    }

    /**
     * Identifies the caller.
     *
     * <p>Honours X-Forwarded-For so the limit follows the real client rather
     * than the proxy. That header is client-supplied, so this is only
     * trustworthy behind a proxy that overwrites it.
     */
    private static String clientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            int comma = forwarded.indexOf(',');
            return (comma > 0 ? forwarded.substring(0, comma) : forwarded).trim();
        }
        return request.getRemoteAddr();
    }

    /** Entries are replaced rather than evicted; a stale one is overwritten on next use. */
    private static final class Window {
        private final Instant started;
        private final AtomicInteger count = new AtomicInteger();

        private Window(Instant started) {
            this.started = started;
        }

        private boolean startedBefore(Instant cutoff) {
            return started.isBefore(cutoff);
        }
    }
}
