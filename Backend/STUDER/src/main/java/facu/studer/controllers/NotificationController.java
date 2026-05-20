package facu.studer.controllers;

import facu.studer.DTOs.discussions.MessageResponseDTO;
import facu.studer.DTOs.notification.NotificationPageResponseDTO;
import facu.studer.entities.LinkedType;
import facu.studer.security.SecurityUtils;
import facu.studer.services.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for notification operations.
 * All endpoints require authentication via JWT token.
 * Users can only access their own notifications.
 */
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final SecurityUtils securityUtils;

    public NotificationController(NotificationService notificationService, SecurityUtils securityUtils) {
        this.notificationService = notificationService;
        this.securityUtils = securityUtils;
    }

    /**
     * Gets paginated notifications for the authenticated user.
     * Supports optional filters: type, read status, and date range.
     * Notifications with availableAt in the future are excluded.
     *
     * @param page     page number (0-based, default 0)
     * @param type     optional LinkedType filter
     * @param read     optional read status filter
     * @param lastDays optional filter for notifications within last N days
     * @return paginated notification response
     */
    @GetMapping
    public ResponseEntity<NotificationPageResponseDTO> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) LinkedType type,
            @RequestParam(required = false) Boolean read,
            @RequestParam(required = false) Integer lastDays) {

        String username = securityUtils.requireCurrentUsername();
        NotificationPageResponseDTO response = notificationService
                .getNotifications(username, page, type, read, lastDays);
        return ResponseEntity.ok(response);
    }

    /**
     * Marks a notification as read for the authenticated user.
     * Only the notification owner can mark it as read.
     *
     * @param id the UserNotification ID
     * @return success/error message
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<MessageResponseDTO> markAsRead(@PathVariable Long id) {
        String username = securityUtils.requireCurrentUsername();
        MessageResponseDTO response = notificationService.markAsRead(username, id);
        return ResponseEntity.ok(response);
    }
}

