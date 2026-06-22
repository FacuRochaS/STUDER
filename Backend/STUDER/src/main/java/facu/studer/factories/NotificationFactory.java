package facu.studer.factories;

import facu.studer.entities.LinkedType;
import facu.studer.entities.users.User;
import facu.studer.entities.notifications.Notification;

import java.time.LocalDateTime;

/**
 * Factory for building notifications by type.
 */
public final class NotificationFactory {
    private NotificationFactory() {
    }



    public static Notification buildMessageNotification(User sender, boolean areFriends) {
        String messageKey = areFriends ? "message.new_message" : "message.new_request";
        return Notification.builder()
                .title(messageKey + "_title")
                .message(messageKey + "_message")
                .type(LinkedType.USER)
                .linkedId(sender.getId())
                .createdDatetime(LocalDateTime.now())
                .lastUpdatedDatetime(LocalDateTime.now())
                .isActive(true)
                .build();
    }
}

