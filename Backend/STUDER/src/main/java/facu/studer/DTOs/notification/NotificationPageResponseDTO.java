package facu.studer.DTOs.notification;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Paginated response DTO for notifications.
 */
@Getter
@Setter
@Builder
public class NotificationPageResponseDTO {

    /** List of notifications for the current page. */
    private List<NotificationResponseDTO> notifications;

    /** Total number of notifications matching the filters. */
    private long totalElements;

    /** Whether there are more pages after the current one. */
    private boolean hasMore;

    /** Current page number (0-based). */
    private int currentPage;
}

