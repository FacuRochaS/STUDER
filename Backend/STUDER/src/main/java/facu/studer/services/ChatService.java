package facu.studer.services;

import facu.studer.DTOs.chats.ChatSummaryDTO;
import facu.studer.DTOs.chats.MessageRequestDTO;
import facu.studer.DTOs.chats.MessageResponseDTO;
import facu.studer.entities.messages.DirectMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ChatService {
    List<ChatSummaryDTO> getChats(Long currentUserId);
    Page<MessageResponseDTO> getMessagesByChatId(Long chatId, Pageable pageable);
    MessageResponseDTO sendMessageToUser(Long currentUserId, Long targetUserId, MessageRequestDTO request, MultipartFile file);
    MessageResponseDTO sendMessageToChat(Long currentUserId, Long chatId, MessageRequestDTO request, MultipartFile file);
    void markChatAsRead(Long chatId, Long currentUserId);
}