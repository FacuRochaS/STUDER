package facu.studer.services;

import facu.studer.DTOs.chats.ChatSummaryDTO;
import facu.studer.DTOs.chats.MessageRequestDTO;
import facu.studer.entities.messages.DirectMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ChatService {
    List<ChatSummaryDTO> getChats(Long currentUserId);
    Page<DirectMessage> getMessagesByChatId(Long chatId, Pageable pageable);
    DirectMessage sendMessageToUser(Long currentUserId, Long targetUserId, MessageRequestDTO request);
    DirectMessage sendMessageToChat(Long currentUserId, Long chatId, MessageRequestDTO request);
    void markChatAsRead(Long chatId, Long currentUserId);
}