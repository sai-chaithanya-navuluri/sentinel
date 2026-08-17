package dev.sentinel.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AdminApiKeyFilter extends OncePerRequestFilter {

    @Value("${sentinel.admin-api-key:}")
    private String adminApiKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        if (!request.getRequestURI().startsWith("/admin")) {
            chain.doFilter(request, response);
            return;
        }

        // If no key is configured, admin endpoints are open (local dev convenience —
        // mirrors the same pattern used in the content-core API).
        if (adminApiKey == null || adminApiKey.isBlank()) {
            chain.doFilter(request, response);
            return;
        }

        String providedKey = request.getHeader("X-Admin-Key");
        if (providedKey == null || !constantTimeEquals(providedKey, adminApiKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Missing or invalid X-Admin-Key header\"}");
            return;
        }

        chain.doFilter(request, response);
    }

    /**
     * Constant-time comparison prevents timing attacks — a naive .equals() returns
     * faster on an early mismatch, which can leak how many leading characters were
     * correct if an attacker measures response time across many attempts.
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}