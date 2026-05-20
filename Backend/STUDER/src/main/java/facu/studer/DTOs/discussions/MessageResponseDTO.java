package facu.studer.DTOs.discussions;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

/**
 * Generic response DTO for simple success/error messages.
 * Used for like, favourite, close operations.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MessageResponseDTO {

    /** Whether the operation was successful. */
    private boolean success;

    /** Descriptive message about the result. */
    private String message;
}

