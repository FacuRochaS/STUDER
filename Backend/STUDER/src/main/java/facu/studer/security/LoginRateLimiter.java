package facu.studer.security;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory rate limiter for login attempts.
 */
@Component
public class LoginRateLimiter {
    /**
     * Maximum allowed failed login attempts before blocking.
     */
    private static final int MAX_ATTEMPTS = 5;
    /**
     * Minutes to block after reaching max attempts.
     */
    private static final long BLOCK_MINUTES = 10;
    /**
     * Stores login attempts by key (IP:username).
     */
    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    /**
     * Checks if the key is currently blocked.
     *
     * @param key the identifier (IP:username)
     * @return true if blocked, false otherwise
     */
    public boolean isBlocked(String key) {
        Attempt attempt = attempts.get(key);
        return attempt != null && attempt.blockedUntil != null && LocalDateTime.now().isBefore(attempt.blockedUntil);
    }

    /**
     * Registers a failed login attempt for the key.
     *
     * @param key the identifier (IP:username)
     */
    public void loginFailed(String key) {
        Attempt attempt = attempts.computeIfAbsent(key, k -> new Attempt());
        attempt.count++;
        if (attempt.count >= MAX_ATTEMPTS) {
            attempt.blockedUntil = LocalDateTime.now().plusMinutes(BLOCK_MINUTES);
            attempt.count = 0;
        }
    }

    /**
     * Resets the failed login attempts for the key after a successful login.
     *
     * @param key the identifier (IP:username)
     */
    public void loginSucceeded(String key) {
        attempts.remove(key);
    }

    /**
     * Stores attempt count and block time.
     */
    private static final class Attempt {
        /**
         * Number of failed attempts.
         */
        private int count = 0;
        /**
         * Blocked until this time, or null if not blocked.
         */
        private LocalDateTime blockedUntil = null;
    }
}
