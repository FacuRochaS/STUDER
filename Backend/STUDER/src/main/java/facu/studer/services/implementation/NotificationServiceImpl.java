package facu.studer.services.implementation;

import facu.studer.DTOs.discussions.MessageResponseDTO;
import facu.studer.DTOs.notification.NotificationPageResponseDTO;
import facu.studer.DTOs.notification.NotificationResponseDTO;
import facu.studer.entities.LinkedType;
import facu.studer.entities.notifications.Notification;
import facu.studer.entities.notifications.UserNotification;
import facu.studer.exceptions.ResourceNotFoundException;
import facu.studer.exceptions.UnauthorizedOperationException;
import facu.studer.mappers.NotificationMapper;
import facu.studer.repositories.UserNotificationRepository;
import facu.studer.services.NotificationService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final int PAGE_SIZE = 10;

    private final UserNotificationRepository userNotificationRepository;

    public NotificationServiceImpl(UserNotificationRepository userNotificationRepository) {
        this.userNotificationRepository = userNotificationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationPageResponseDTO getNotifications(
            String username,
            int page,
            LinkedType type,
            Boolean read,
            Integer lastDays) {

        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("notification.createdDatetime").descending());

        Specification<UserNotification> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<UserNotification, Notification> notificationJoin = root.join("notification");

            // Basic conditions
            predicates.add(cb.equal(root.get("user").get("username"), username));
            predicates.add(cb.isTrue(root.get("isActive")));
            predicates.add(cb.isTrue(notificationJoin.get("isActive")));

            // Optional filters
            if (type != null) {
                predicates.add(cb.equal(notificationJoin.get("type"), type));
            }
            if (read != null) {
                predicates.add(cb.equal(root.get("read"), read));
            }
            if (lastDays != null) {
                LocalDateTime since = LocalDateTime.now().minusDays(lastDays);
                predicates.add(cb.greaterThanOrEqualTo(notificationJoin.get("createdDatetime"), since));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<UserNotification> notificationPage = userNotificationRepository.findAll(spec, pageable);

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

    @Override
    @Transactional
    public MessageResponseDTO markAsRead(String username, Long userNotificationId) {
        UserNotification userNotification = userNotificationRepository
                .findByIdWithNotification(userNotificationId)
                .orElseThrow(() -> new ResourceNotFoundException("notification.not_found"));

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