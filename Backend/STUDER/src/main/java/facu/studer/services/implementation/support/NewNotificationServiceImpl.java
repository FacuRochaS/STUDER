package facu.studer.services.implementation.support;

import facu.studer.DTOs.MessageResponseDTO;
import facu.studer.entities.LinkedType;
import facu.studer.entities.notifications.Notification;
import facu.studer.entities.notifications.UserNotification;
import facu.studer.repositories.NotificationRepository;
import facu.studer.repositories.UserNotificationRepository;
import facu.studer.repositories.UserRepository;
import facu.studer.services.support.NewNotificationService;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class NewNotificationServiceImpl implements NewNotificationService {
    private final UserNotificationRepository userNotificationRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    private final MessageSource messageSource;

    public NewNotificationServiceImpl(UserNotificationRepository userNotificationRepository, NotificationRepository notificationRepository, UserRepository userRepository, MessageSource messageSource) {
        this.userNotificationRepository = userNotificationRepository;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;

        this.messageSource = messageSource;
    }

    /**
     * Creates a new notification for a list of users.
     *
     * @param userIds  the list of user IDs
     * @param title    the notification title
     * @param message  the message
     * @param type     type of notification
     * @param linkedId linked id
     * @return success/error message
     */
    @Override
    public MessageResponseDTO createNotificationList(List<Long> userIds, String title, String message, LinkedType type, Long linkedId) {
        // Create the notification entity
        var notification = Notification.builder()
                .title(resolveMessage(title))
                .message(resolveMessage(message))
                .type(type)
                .linkedId(linkedId)
                .build();

        // Save the notification
        var savedNotification = notificationRepository.save(notification);

        // Create UserNotification for each user
        userIds.forEach(userId -> {
            var actualUser = userRepository.findById(userId);
            if(actualUser.isPresent()){
                var userNotification = UserNotification.builder()
                        .user(actualUser.get())
                        .notification(savedNotification)
                        .read(false)
                        .isActive(true)
                        .build();
                userNotificationRepository.save(userNotification);
            }


        });

        return MessageResponseDTO.builder()
                .success(true)
                .message("Notification created and sent to users successfully.")
                .build();
    }


    /**
     * Creates a new notification for a list of users.
     *
     * @param userId  the list of user IDs
     * @param title    the notification title
     * @param message  the message
     * @param type     type of notification
     * @param linkedId linked id
     * @return success/error message
     */
    @Override
    public MessageResponseDTO createNotification(Long userId, String title, String message, LinkedType type, Long linkedId) {
        // Create the notification entity
        var notification = Notification.builder()
                .title(resolveMessage(title))
                .message(resolveMessage(message))
                .type(type)
                .linkedId(linkedId)
                .createdDatetime(LocalDateTime.now())
                .lastUpdatedDatetime(LocalDateTime.now())
                .isActive(true)
                .build();

        // Save the notification
        var savedNotification = notificationRepository.save(notification);


        var actualUser = userRepository.findById(userId);
        if(actualUser.isPresent()){
            var userNotification = UserNotification.builder()
                    .user(actualUser.get())
                    .notification(savedNotification)
                    .read(false)
                    .isActive(true)
                    .createdDatetime(LocalDateTime.now())
                    .lastUpdatedDatetime(LocalDateTime.now())
                    .build();
            userNotificationRepository.save(userNotification);
        }




        return MessageResponseDTO.builder()
                .success(true)
                .message("Notification created and sent to users successfully.")
                .build();
    }


    private String resolveMessage(String messageKey) {
        if (messageKey == null) {
            return "An unknown error occurred.";
        }
        Locale locale = LocaleContextHolder.getLocale();
        try {
            return messageSource.getMessage(messageKey, null, locale);
        } catch (Exception e) {
            // Si la clave no se encuentra, devolver la clave misma.
            return messageKey;
        }
    }


}
