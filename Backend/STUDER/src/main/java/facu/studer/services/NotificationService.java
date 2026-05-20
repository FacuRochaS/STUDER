package facu.studer.services;

import facu.studer.DTOs.discussions.MessageResponseDTO;
import facu.studer.DTOs.notification.NotificationPageResponseDTO;
import facu.studer.entities.LinkedType;

/**
 * Service interface for notification operations.
 * Handles paginated retrieval with filters and read status management.
 */
public interface NotificationService {

    /**
     * Gets paginated notifications for the authenticated user with optional filters.
     * Only returns notifications where availableAt <= now.
     *
     * @param username the authenticated username
     * @param page     page number (0-based)
     * @param type     optional LinkedType filter
     * @param read     optional read status filter
     * @param lastDays optional filter for notifications within last N days
     * @return paginated notification response
     */
    NotificationPageResponseDTO getNotifications(
            String username,
            int page,
            LinkedType type,
            Boolean read,
            Integer lastDays);

    /**
     * Marks a notification as read for the authenticated user.
     *
     * @param username           the authenticated username
     * @param userNotificationId the UserNotification ID
     * @return success/error message
     */
    MessageResponseDTO markAsRead(String username, Long userNotificationId);
}

