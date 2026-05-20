package facu.studer.services.implementation;

import facu.studer.entities.RefreshToken;
import facu.studer.entities.User;
import facu.studer.exceptions.InvalidRefreshTokenException;
import facu.studer.exceptions.TokenReuseDetectedException;
import facu.studer.repositories.RefreshTokenRepository;
import facu.studer.services.RefreshTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * Implementation of RefreshTokenService.
 * Handles refresh token generation, validation, rotation, and revocation.
 */
@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureRandom secureRandom;
    private final long refreshTokenValidityDays;

    public RefreshTokenServiceImpl(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${app.security.refresh-token-validity-days:7}") long refreshTokenValidityDays) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.secureRandom = new SecureRandom();
        this.refreshTokenValidityDays = refreshTokenValidityDays;
    }

    @Override
    @Transactional
    public String generateRefreshToken(User user, String userAgent, String ipAddress) {
        String plainToken = generateSecureToken();
        String tokenHash = hashToken(plainToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(refreshTokenValidityDays))
                .userAgent(truncate(userAgent, 500))
                .ipAddress(truncate(ipAddress, 45))
                .build();

        refreshTokenRepository.save(refreshToken);
        return plainToken;
    }

    @Override
    @Transactional(readOnly = true)
    public RefreshToken validateRefreshToken(String plainToken) {
        String tokenHash = hashToken(plainToken);

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidRefreshTokenException("auth.refresh_token_invalid"));

        // Check for token reuse (already rotated/revoked)
        if (refreshToken.isRevoked()) {
            // This could be a token theft attack - revoke all user tokens
            throw new TokenReuseDetectedException("auth.token_reuse_detected", refreshToken.getUser().getId());
        }

        if (refreshToken.isExpired()) {
            throw new InvalidRefreshTokenException("auth.refresh_token_expired");
        }

        return refreshToken;
    }

    @Override
    @Transactional
    public String rotateRefreshToken(RefreshToken oldToken, String userAgent, String ipAddress) {
        // Generate new token
        String newPlainToken = generateSecureToken();
        String newTokenHash = hashToken(newPlainToken);

        // Mark old token as replaced
        oldToken.setRevokedAt(LocalDateTime.now());
        oldToken.setReplacedByHash(newTokenHash);
        refreshTokenRepository.save(oldToken);

        // Create new token
        RefreshToken newRefreshToken = RefreshToken.builder()
                .user(oldToken.getUser())
                .tokenHash(newTokenHash)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(refreshTokenValidityDays))
                .userAgent(truncate(userAgent, 500))
                .ipAddress(truncate(ipAddress, 45))
                .build();

        refreshTokenRepository.save(newRefreshToken);
        return newPlainToken;
    }

    @Override
    @Transactional
    public void revokeRefreshToken(String plainToken) {
        String tokenHash = hashToken(plainToken);
        refreshTokenRepository.findByTokenHash(tokenHash)
                .ifPresent(token -> {
                    token.setRevokedAt(LocalDateTime.now());
                    refreshTokenRepository.save(token);
                });
    }

    @Override
    @Transactional
    public void revokeAllUserTokens(User user) {
        refreshTokenRepository.revokeAllByUser(user, LocalDateTime.now());
    }

    @Override
    @Transactional
    public int cleanupExpiredTokens() {
        return refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }

    /**
     * Generates a cryptographically secure random token.
     */
    private String generateSecureToken() {
        byte[] tokenBytes = new byte[32]; // 256 bits
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    /**
     * Hashes a token using SHA-256.
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Truncates a string to the specified max length.
     */
    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }
}

