package facu.studer.exeptions;

/**
 * Exception thrown when a refresh token is invalid, expired, or revoked.
 */
public class InvalidRefreshTokenException extends RuntimeException {

    public InvalidRefreshTokenException(String messageKey) {
        super(messageKey);
    }
}

