package facu.studer.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Entity representing a refresh token for JWT authentication.
 * Stores hashed tokens for security.
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
public class RefreshToken {

    /**
     * Auto-generated identifier.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reference to the user who owns this token.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    /**
     * SHA-256 hash of the refresh token.
     * Never store the plain token.
     */
    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    /**
     * When the token was created.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * When the token expires.
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * When the token was revoked (null if still valid).
     */
    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    /**
     * Hash of the token that replaced this one (for rotation tracking).
     */
    @Column(name = "replaced_by_hash")
    private String replacedByHash;

    /**
     * User agent of the client that created this token.
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * IP address of the client that created this token.
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * Checks if the token is valid (not revoked and not expired).
     */
    public boolean isValid() {
        return revokedAt == null && expiresAt.isAfter(LocalDateTime.now());
    }

    /**
     * Checks if the token has been revoked.
     */
    public boolean isRevoked() {
        return revokedAt != null;
    }

    /**
     * Checks if the token has expired.
     */
    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }
}

