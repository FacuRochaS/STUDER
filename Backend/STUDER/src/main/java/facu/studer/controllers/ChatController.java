package facu.studer.controllers;

import facu.studer.DTOs.chats.ChatSummaryDTO;
import facu.studer.DTOs.chats.MessageRequestDTO;
import facu.studer.entities.messages.DirectMessage;
import facu.studer.security.SecurityUtils;
import facu.studer.services.ChatService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SecurityUtils securityUtils;



    @GetMapping
    public ResponseEntity<List<ChatSummaryDTO>> getChats() {
        Long userId = securityUtils.requireCurrentUserId();
        List<ChatSummaryDTO> chats = chatService.getChats(userId);
        return ResponseEntity.ok(chats);
    }

    @GetMapping("/{chatId}/messages")
    public ResponseEntity<Page<DirectMessage>> getMessagesByChatId(
            @PathVariable Long chatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        // Envia un PageRequest. Los ordenamos descendentemente en el Repository.
        Page<DirectMessage> messages = chatService.getMessagesByChatId(chatId, PageRequest.of(page, size));
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/user/{targetUserId}")
    public ResponseEntity<DirectMessage> sendMessageToUser(
            @PathVariable Long targetUserId,
            @RequestBody MessageRequestDTO request) {
        Long userId = securityUtils.requireCurrentUserId();
        DirectMessage message = chatService.sendMessageToUser(userId, targetUserId, request);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/{chatId}/messages")
    public ResponseEntity<DirectMessage> sendMessageToChat(
            @PathVariable Long chatId,
            @RequestBody MessageRequestDTO request) {
        Long userId = securityUtils.requireCurrentUserId();
        DirectMessage message = chatService.sendMessageToChat(userId, chatId, request);
        return ResponseEntity.ok(message);
    }

    @PutMapping("/{chatId}/read")
    public ResponseEntity<Void> markChatAsRead(@PathVariable Long chatId) {
        Long userId = securityUtils.requireCurrentUserId();
        chatService.markChatAsRead(chatId, userId);
        return ResponseEntity.noContent().build();
    }
}