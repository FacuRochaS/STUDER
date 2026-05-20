package facu.studer.exceptions;

/**
 * Exception thrown when a user tries to perform an operation they are not authorized to do.
 * For example, trying to delete another user's account.
 */
public class UnauthorizedOperationException extends RuntimeException {

    public UnauthorizedOperationException(String messageKey) {
        super(messageKey);
    }
}

