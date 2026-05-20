package facu.studer.mappers;

import facu.studer.DTOs.notification.NotificationResponseDTO;
import facu.studer.entities.notifications.Notification;
import facu.studer.entities.notifications.UserNotification;

/**
 * Mapper for Notification entities to DTOs.
 */
public final class NotificationMapper {

    private NotificationMapper() { }

    /**
     * Maps a UserNotification (with its associated Notification) to a NotificationResponseDTO.
     *
     * @param userNotification the user notification entity
     * @return the response DTO
     */
    public static NotificationResponseDTO toResponseDTO(UserNotification userNotification) {
        if (userNotification == null || userNotification.getNotification() == null) {
            return null;
        }
        Notification notification = userNotification.getNotification();
        return NotificationResponseDTO.builder()
                .id(userNotification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .createdAt(notification.getCreatedDatetime())
                .type(notification.getType())
                .read(userNotification.isRead())
                .build();
    }
}

