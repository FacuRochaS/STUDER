package facu.studer.services.implementation;

import facu.studer.DTOs.discussions.MessageResponseDTO;
import facu.studer.DTOs.notification.NotificationPageResponseDTO;
import facu.studer.DTOs.notification.NotificationResponseDTO;
import facu.studer.entities.LinkedType;
import facu.studer.entities.notifications.UserNotification;
import facu.studer.exceptions.ResourceNotFoundException;
import facu.studer.exceptions.UnauthorizedOperationException;
import facu.studer.mappers.NotificationMapper;
import facu.studer.repositories.UserNotificationRepository;
import facu.studer.services.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of NotificationService.
 * Uses only UserNotificationRepository (respects 1-repo-per-service).
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private static final int PAGE_SIZE = 10;

    private final UserNotificationRepository userNotificationRepository;

    public NotificationServiceImpl(UserNotificationRepository userNotificationRepository) {
        this.userNotificationRepository = userNotificationRepository;
    }

    /**
     * Gets paginated notifications for a user with optional filters.
     * Notifications with availableAt in the future are excluded entirely.
     */
    @Override
    @Transactional(readOnly = true)
    public NotificationPageResponseDTO getNotifications(
            String username,
            int page,
            LinkedType type,
            Boolean read,
            Integer lastDays) {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime since = lastDays != null ? now.minusDays(lastDays) : null;
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);

        Page<UserNotification> notificationPage = userNotificationRepository
                .findFilteredNotifications(username, now, type, read, since, pageable);

        List<NotificationResponseDTO> dtos = notificationPage.getContent().stream()
                .map(NotificationMapper::toResponseDTO)
                .collect(Collectors.toList());

        return NotificationPageResponseDTO.builder()
                .notifications(dtos)
                .totalElements(notificationPage.getTotalElements())
                .hasMore(notificationPage.hasNext())
                .currentPage(page)
                .build();
    }

    /**
     * Marks a notification as read. Validates that the notification belongs to the user.
     */
    @Override
    @Transactional
    public MessageResponseDTO markAsRead(String username, Long userNotificationId) {
        UserNotification userNotification = userNotificationRepository
                .findByIdWithNotification(userNotificationId)
                .orElseThrow(() -> new ResourceNotFoundException("notification.not_found"));

        // Validate ownership
        if (!userNotification.getUser().getUsername().equals(username)) {
            throw new UnauthorizedOperationException("notification.unauthorized");
        }

        userNotification.setRead(true);
        userNotification.setReadAt(LocalDateTime.now());
        userNotification.setLastUpdatedDatetime(LocalDateTime.now());
        userNotificationRepository.save(userNotification);

        return MessageResponseDTO.builder()
                .success(true)
                .message("notification.marked_read")
                .build();
    }
}

