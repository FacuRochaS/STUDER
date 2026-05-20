package facu.studer.exceptions;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Standard error response for API errors.
 */
@Data
@Builder
public class ApiError {
    /**
     * Timestamp of the error.
     */
    private LocalDateTime timestamp;
    /**
     * HTTP status code.
     */
    private int status;
    /**
     * Error type.
     */
    private String error;
    /**
     * Error code (for i18n key).
     */
    private String code;
    /**
     * Resolved error message.
     */
    private String message;
    /**
     * Request path where the error occurred.
     */
    private String path;
}
