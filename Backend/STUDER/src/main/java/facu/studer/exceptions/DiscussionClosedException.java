package facu.studer.exceptions;

/**
 * Exception thrown when an action is attempted on a closed discussion.
 */
public class DiscussionClosedException extends RuntimeException {

    public DiscussionClosedException(String messageKey) {
        super(messageKey);
    }
}

