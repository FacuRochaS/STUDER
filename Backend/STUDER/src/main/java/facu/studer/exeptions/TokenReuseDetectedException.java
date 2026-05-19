package facu.studer.exeptions;

import lombok.Getter;

/**
 * Exception thrown when a reused (already rotated) refresh token is detected.
 * This indicates a potential token theft attack.
 */
@Getter
public class TokenReuseDetectedException extends RuntimeException {

    private final Long userId;

    public TokenReuseDetectedException(String messageKey, Long userId) {
        super(messageKey);
        this.userId = userId;
    }

}

