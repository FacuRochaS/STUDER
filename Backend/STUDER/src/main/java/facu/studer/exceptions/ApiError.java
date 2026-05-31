package facu.studer.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard error response for API errors.
 * Includes optional field-specific validation errors.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // No incluir campos nulos en el JSON
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
     * Error type (e.g., "Validation Error", "Unauthorized").
     */
    private String error;
    /**
     * Error code (for i18n key or specific error type).
     */
    private String code;
    /**
     * General resolved error message.
     */
    private String message;
    /**
     * Request path where the error occurred.
     */
    private String path;
    /**
     * Map of field-specific validation errors.
     * Only included for validation errors (400 Bad Request).
     */
    private Map<String, String> fieldErrors;
}