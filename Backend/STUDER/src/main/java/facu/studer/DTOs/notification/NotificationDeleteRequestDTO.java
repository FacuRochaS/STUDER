package facu.studer.DTOs.notification;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for Notification delete request.
 */
@Getter
@Setter
@Builder
public class NotificationDeleteRequestDTO {
    private Long id;//user notification id
}

