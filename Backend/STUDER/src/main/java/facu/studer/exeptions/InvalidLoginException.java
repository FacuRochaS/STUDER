package facu.studer.exeptions;

/**
 * Exception for invalid login attempts.
 */
public class InvalidLoginException extends RuntimeException {
    /**
     * Constructor for InvalidLoginException.
     *
     * @param message the error message
     */
    public InvalidLoginException(String message) {
        super(message);
    }
}
