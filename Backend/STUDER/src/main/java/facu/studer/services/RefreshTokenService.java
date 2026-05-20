package facu.studer.services;

import facu.studer.entities.RefreshToken;
import facu.studer.entities.User;

/**
 * Service interface for managing refresh tokens.
 */
public interface RefreshTokenService {

    /**
     * Generates a new refresh token for a user.
     *
     * @param user      the user
     * @param userAgent the client user agent
     * @param ipAddress the client IP address
     * @return the plain refresh token (to be sent to client)
     */
    String generateRefreshToken(User user, String userAgent, String ipAddress);

    /**
     * Validates a refresh token and returns the associated entity.
     *
     * @param plainToken the plain refresh token
     * @return the RefreshToken entity
     * @throws facu.login.exception.InvalidRefreshTokenException if invalid
     * @throws facu.login.exception.TokenReuseDetectedException  if reuse detected
     */
    RefreshToken validateRefreshToken(String plainToken);

    /**
     * Rotates a refresh token: revokes the old one and creates a new one.
     *
     * @param oldToken  the old RefreshToken entity
     * @param userAgent the client user agent
     * @param ipAddress the client IP address
     * @return the new plain refresh token
     */
    String rotateRefreshToken(RefreshToken oldToken, String userAgent, String ipAddress);

    /**
     * Revokes a specific refresh token.
     *
     * @param plainToken the plain refresh token
     */
    void revokeRefreshToken(String plainToken);

    /**
     * Revokes all refresh tokens for a user (logout from all devices).
     *
     * @param user the user
     */
    void revokeAllUserTokens(User user);

    /**
     * Cleans up expired tokens from the database.
     *
     * @return number of deleted tokens
     */
    int cleanupExpiredTokens();
}

