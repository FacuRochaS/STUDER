package facu.studer.controllers;

import facu.studer.DTOs.chats.ChatSummaryDTO;
import facu.studer.DTOs.chats.MessageRequestDTO;
import facu.studer.DTOs.chats.MessageResponseDTO;
import facu.studer.entities.messages.DirectMessage;
import facu.studer.security.SecurityUtils;
import facu.studer.services.ChatService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
/**
 * REST controller for managing chat conversations, messages, and unread statuses.
 */
@RestController
@RequestMapping("/api/v1/chats")
public class ChatController {

    private final ChatService chatService;
    private final SecurityUtils securityUtils;

    /**
     * Constructs a ChatController with the required business logic services and security utilities.
     *
     * @param chatService   the service handling chat operations
     * @param securityUtils the utility class for extracting user authentication details
     */
    public ChatController(ChatService chatService, SecurityUtils securityUtils) {
        this.chatService = chatService;
        this.securityUtils = securityUtils;
    }

    /**
     * Retrieves a summary list of all chats belonging to the authenticated user.
     *
     * @return a ResponseEntity containing the list of chat summaries
     */
    @GetMapping
    public ResponseEntity<List<ChatSummaryDTO>> getChats() {
        Long userId = securityUtils.requireCurrentUserId();
        List<ChatSummaryDTO> chats = chatService.getChats(userId);
        return ResponseEntity.ok(chats);
    }

    /**
     * Retrieves a paginated slice of messages from a specific chat.
     *
     * @param chatId the unique identifier of the chat
     * @param page   the zero-based page index
     * @param size   the size of the page to be returned
     * @return a ResponseEntity containing the page of message details
     */
    @GetMapping("/{chatId}/messages")
    public ResponseEntity<Page<MessageResponseDTO>> getMessagesByChatId(
            @PathVariable Long chatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        // Envia un PageRequest. Los ordenamos descendentemente en el Repository.
        Page<MessageResponseDTO> messages = chatService.getMessagesByChatId(chatId, PageRequest.of(page, size));
        return ResponseEntity.ok(messages);
    }


    /**
     * Sends a message to a specific user, creating a chat if it doesn't already exist.
     *
     * @param targetUserId the unique identifier of the recipient user
     * @param request      the message text and configurations
     * @param file         an optional multipart media file attachment
     * @return a ResponseEntity containing the created message details
     */
    @PostMapping(value = "/user/{targetUserId}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<MessageResponseDTO> sendMessageToUser(
            @PathVariable Long targetUserId,
            @Valid @RequestPart("request") MessageRequestDTO request,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        Long currentUserId = securityUtils.requireCurrentUserId();
        MessageResponseDTO message = chatService.sendMessageToUser(currentUserId, targetUserId, request, file);
        return ResponseEntity.ok(message);
    }


    /**
     * Sends a message into an existing chat conversation.
     *
     * @param chatId  the unique identifier of the target chat
     * @param request the message text and configurations
     * @param file    an optional multipart media file attachment
     * @return a ResponseEntity containing the created message details
     */
    @PostMapping(value = "/{chatId}/messages", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<MessageResponseDTO> sendMessageToChat(
            @PathVariable Long chatId,
            @Valid @RequestPart("request") MessageRequestDTO request,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        Long currentUserId = securityUtils.requireCurrentUserId();
        MessageResponseDTO message = chatService.sendMessageToChat(currentUserId, chatId, request, file);
        return ResponseEntity.ok(message);
    }

    /**
     * Marks all unread messages in the specified chat as read for the authenticated user.
     *
     * @param chatId the unique identifier of the chat
     * @return a ResponseEntity with 204 No Content status
     */
    @PutMapping("/{chatId}/read")
    public ResponseEntity<Void> markChatAsRead(@PathVariable Long chatId) {
        Long userId = securityUtils.requireCurrentUserId();
        chatService.markChatAsRead(chatId, userId);
        return ResponseEntity.noContent().build();
    }
}