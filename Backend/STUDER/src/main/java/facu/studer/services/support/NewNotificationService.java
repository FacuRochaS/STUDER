package facu.studer.services.support;

import facu.studer.DTOs.MessageDTO;
import facu.studer.entities.LinkedType;

import java.util.List;

public interface NewNotificationService {
    /**
     * Creates a new notification for a list of users.
     *
     * @param userIds the list of user IDs
     * @param title  the notification title
     * @param message the message
     * @param type   type of notification
     * @param linkedId   linked id
     * @return success/error message
     */
    MessageDTO createNotificationList(
            List<Long> userIds,
            String title,
            String message,
            LinkedType type,
            Long linkedId );

    /**
     * Creates a new notification for a list of users.
     *
     * @param userId the user IDs
     * @param title  the notification title
     * @param message the message
     * @param type   type of notification
     * @param linkedId   linked id
     * @return success/error message
     */
    MessageDTO createNotification(
            Long userId,
            String title,
            String message,
            LinkedType type,
            Long linkedId );

    MessageDTO createParameterNotification(
            Long userId,
            String title,String[] tParams,
            String message,String[] mParams,
            LinkedType type,
            Long linkedId );


    }
