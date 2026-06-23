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

@RestController
@RequestMapping("/api/v1/chats")
public class ChatController {

    private final ChatService chatService;
    private final SecurityUtils securityUtils;

    public ChatController(ChatService chatService, SecurityUtils securityUtils) {
        this.chatService = chatService;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    public ResponseEntity<List<ChatSummaryDTO>> getChats() {
        Long userId = securityUtils.requireCurrentUserId();
        List<ChatSummaryDTO> chats = chatService.getChats(userId);
        return ResponseEntity.ok(chats);
    }

    @GetMapping("/{chatId}/messages")
    public ResponseEntity<Page<MessageResponseDTO>> getMessagesByChatId(
            @PathVariable Long chatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        // Envia un PageRequest. Los ordenamos descendentemente en el Repository.
        Page<MessageResponseDTO> messages = chatService.getMessagesByChatId(chatId, PageRequest.of(page, size));
        return ResponseEntity.ok(messages);
    }

    @PostMapping(value = "/user/{targetUserId}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<MessageResponseDTO> sendMessageToUser(
            @PathVariable Long targetUserId,
            @Valid @RequestPart("request") MessageRequestDTO request,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        Long currentUserId = securityUtils.requireCurrentUserId();
        MessageResponseDTO message = chatService.sendMessageToUser(currentUserId, targetUserId, request, file);
        return ResponseEntity.ok(message);
    }

    @PostMapping(value = "/{chatId}/messages", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<MessageResponseDTO> sendMessageToChat(
            @PathVariable Long chatId,
            @Valid @RequestPart("request") MessageRequestDTO request,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        Long currentUserId = securityUtils.requireCurrentUserId();
        MessageResponseDTO message = chatService.sendMessageToChat(currentUserId, chatId, request, file);
        return ResponseEntity.ok(message);
    }

    @PutMapping("/{chatId}/read")
    public ResponseEntity<Void> markChatAsRead(@PathVariable Long chatId) {
        Long userId = securityUtils.requireCurrentUserId();
        chatService.markChatAsRead(chatId, userId);
        return ResponseEntity.noContent().build();
    }
}