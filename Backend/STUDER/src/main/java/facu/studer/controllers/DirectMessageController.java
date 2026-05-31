package facu.studer.controllers;

import facu.studer.DTOs.discussions.MessageResponseDTO;
import facu.studer.DTOs.messages.ChatListPageResponseDTO;
import facu.studer.DTOs.messages.DirectMessagePageResponseDTO;
import facu.studer.DTOs.messages.DirectMessageRequestDTO;
import facu.studer.DTOs.messages.DirectMessageResponseDTO;
import facu.studer.security.SecurityUtils;
import facu.studer.services.DirectMessageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for direct message operations.
 * All endpoints require authentication via JWT token.
 * Users can message confirmed friends and receive message requests from non-friends.
 */
@RestController
@RequestMapping("/api/v1/messages")
public class DirectMessageController {

    private final DirectMessageService directMessageService;
    private final SecurityUtils securityUtils;

    public DirectMessageController(DirectMessageService directMessageService, SecurityUtils securityUtils) {
        this.directMessageService = directMessageService;
        this.securityUtils = securityUtils;
    }

    /**
     * Sends a direct message to another user.
     * If users are friends: creates a regular message notification.
     * If users are not friends: creates a message request notification.
     *
     * @param request the message request (receiverId, content, link, replyToId optional)
     * @return the sent message response
     */
    @PostMapping
    public ResponseEntity<DirectMessageResponseDTO> sendMessage(
            @Valid @RequestBody DirectMessageRequestDTO request) {
        String username = securityUtils.requireCurrentUsername();
        DirectMessageResponseDTO response = directMessageService.sendMessage(username, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Gets paginated messages from a specific user (chat with them).
     * Returns last 30 messages ordered by sent time.
     * Only accessible if both users are confirmed friends.
     *
     * @param userId the ID of the other user
     * @param page   page number (0-based, default 0)
     * @return paginated messages
     */
    @GetMapping("/chats/{userId}")
    public ResponseEntity<DirectMessagePageResponseDTO> getChatById(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page) {
        String username = securityUtils.requireCurrentUsername();
        DirectMessagePageResponseDTO response = directMessageService.getChatById(username, userId, page);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets all chats for the authenticated user.
     * Only includes chats with confirmed friends.
     * Shows last message, unread count, etc.
     *
     * @param page page number (0-based, default 0)
     * @return paginated chat list
     */
    @GetMapping("/chats")
    public ResponseEntity<ChatListPageResponseDTO> getAllChats(
            @RequestParam(defaultValue = "0") int page) {
        String username = securityUtils.requireCurrentUsername();
        ChatListPageResponseDTO response = directMessageService.getAllChats(username, page);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets message requests (first messages from non-friends).
     * Users can accept these requests by sending a reply or following the sender.
     *
     * @param page page number (0-based, default 0)
     * @return paginated message requests
     */
    @GetMapping("/requests")
    public ResponseEntity<DirectMessagePageResponseDTO> getMessageRequests(
            @RequestParam(defaultValue = "0") int page) {
        String username = securityUtils.requireCurrentUsername();
        DirectMessagePageResponseDTO response = directMessageService.getMessageRequests(username, page);
        return ResponseEntity.ok(response);
    }

    /**
     * Marks all messages from a specific user as read.
     *
     * @param senderId the ID of the sender
     * @return success message
     */
    @PatchMapping("/mark-as-read/{senderId}")
    public ResponseEntity<MessageResponseDTO> markAsRead(@PathVariable Long senderId) {
        String username = securityUtils.requireCurrentUsername();
        MessageResponseDTO response = directMessageService.markAsRead(username, senderId);
        return ResponseEntity.ok(response);
    }
}

