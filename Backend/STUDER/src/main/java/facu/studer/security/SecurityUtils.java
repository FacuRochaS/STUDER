package facu.studer.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Utility class for security-related operations.
 * Handles cookies, IP extraction, and authentication context.
 */
@Component
public class SecurityUtils {

    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    private final boolean secureCookies;
    private final int refreshTokenValidityDays;

    public SecurityUtils(
            @Value("${app.security.secure-cookies:false}") boolean secureCookies,
            @Value("${app.security.refresh-token-validity-days:7}") int refreshTokenValidityDays) {
        this.secureCookies = secureCookies;
        this.refreshTokenValidityDays = refreshTokenValidityDays;
    }

    /**
     * Extracts refresh token from cookie.
     */
    public String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> REFRESH_TOKEN_COOKIE.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    /**
     * Adds refresh token as HttpOnly cookie.
     */
    public void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(secureCookies);
        cookie.setPath("/api/v1/auth");
        cookie.setMaxAge(refreshTokenValidityDays * 24 * 60 * 60);
        response.addCookie(cookie);

        // Add SameSite attribute via header
        response.addHeader("Set-Cookie",
                String.format("%s=%s; Path=/api/v1/auth; Max-Age=%d; HttpOnly; %sSameSite=Lax",
                        REFRESH_TOKEN_COOKIE,
                        refreshToken,
                        refreshTokenValidityDays * 24 * 60 * 60,
                        secureCookies ? "Secure; " : ""));
    }

    /**
     * Clears the refresh token cookie.
     */
    public void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(secureCookies);
        cookie.setPath("/api/v1/auth");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    /**
     * Gets client IP address, considering proxy headers.
     */
    public String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Gets the User-Agent header from request.
     */
    public String getUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }

    /**
     * Gets the current authenticated username from SecurityContext.
     *
     * @return the username, or null if not authenticated
     */
    public String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return (String) auth.getPrincipal();
    }

    /**
     * Gets the current authenticated username, throwing exception if not authenticated.
     *
     * @return the username
     * @throws IllegalStateException if not authenticated
     */
    public String requireCurrentUsername() {
        String username = getCurrentUsername();
        if (username == null) {
            throw new IllegalStateException("No authenticated user found");
        }
        return username;
    }
}

