package facu.studer.DTOs.notification;

import facu.studer.entities.LinkedType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO for Notification response.
 */
@Getter
@Setter
@Builder
public class NotificationResponseDTO {
    /** Notification ID. */
    private Long id;

    /** Notification title. */
    private String title;

    /** Notification message. */
    private String message;

    /** Creation timestamp. */
    private LocalDateTime createdAt;

    private LinkedType type;

    private Boolean read;
}
