package facu.studer.repositories;

import facu.studer.entities.RefreshToken;
import facu.studer.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for RefreshToken entity.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Finds a refresh token by its hash.
     */
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * Finds all valid (non-revoked, non-expired) tokens for a user.
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user = :user AND rt.revokedAt IS NULL AND rt.expiresAt > :now")
    List<RefreshToken> findAllValidTokensByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * Finds all tokens for a user (for session management).
     */
    List<RefreshToken> findAllByUser(User user);

    /**
     * Revokes all tokens for a user.
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revokedAt = :now WHERE rt.user = :user AND rt.revokedAt IS NULL")
    int revokeAllByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * Deletes expired tokens (cleanup job).
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Checks if a token hash exists.
     */
    boolean existsByTokenHash(String tokenHash);
}

