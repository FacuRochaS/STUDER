package facu.studer.services.implementation;

import facu.studer.DTOs.chats.ChatSummaryDTO;
import facu.studer.DTOs.chats.LastMessageDTO;
import facu.studer.DTOs.chats.MessageRequestDTO;
import facu.studer.DTOs.user.UserPublicResponseDTO;
import facu.studer.entities.User;
import facu.studer.entities.messages.Chat;
import facu.studer.entities.messages.DirectMessage;
import facu.studer.repositories.ChatRepository;
import facu.studer.repositories.DirectMessageRepository;
import facu.studer.repositories.UserRepository;
import facu.studer.services.ChatService;
import facu.studer.services.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRepository chatRepository;
    private final DirectMessageRepository messageRepository;
    private final UserService userService; // Tu servicio de usuarios inyectado
    private final UserRepository userRepository; // Asumo que tienes este repo para validar entidades

    @Override
    @Transactional(readOnly = true)
    public List<ChatSummaryDTO> getChats(Long currentUserId) {
        List<Chat> userChats = chatRepository.findChatsByUserId(currentUserId);

        return userChats.stream().map(chat -> {
            // 1. Identificar quién es la otra persona en el chat
            Long otherUserId = chat.getUser1().getId().equals(currentUserId)
                    ? chat.getUser2().getId()
                    : chat.getUser1().getId();

            // 2. Obtener los datos del otro usuario con el servicio externo/interno
            UserPublicResponseDTO otherUser = userService.getById(otherUserId);

            // 3. Obtener el último mensaje
            var lastMessageOpt = messageRepository.findFirstByChatIdAndIsActiveTrueOrderByCreatedDatetimeDesc(chat.getId());
            LastMessageDTO lastMessageDTO = lastMessageOpt.map(msg -> LastMessageDTO.builder()
                    .content(msg.getContent())
                    .timestamp(msg.getCreatedDatetime())
                    .isRead(msg.getIsRead())
                    .build()
            ).orElse(null);

            // 4. Armar la respuesta
            return ChatSummaryDTO.builder()
                    .chatId(chat.getId())
                    .otherUser(otherUser)
                    .lastMessage(lastMessageDTO)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DirectMessage> getMessagesByChatId(Long chatId, Pageable pageable) {
        return messageRepository.findByChatIdAndIsActiveTrueOrderByCreatedDatetimeDesc(chatId, pageable);
    }

    @Override
    @Transactional
    public DirectMessage sendMessageToUser(Long currentUserId, Long targetUserId, MessageRequestDTO request) {
        // Chequea si ya existe el chat
        Chat chat = chatRepository.findChatBetweenUsers(currentUserId, targetUserId)
                .orElseGet(() -> {
                    // Si no existe, lo creamos
                    User currentUser = userRepository.findById(currentUserId).orElseThrow();
                    User targetUser = userRepository.findById(targetUserId).orElseThrow();

                    Chat newChat = Chat.builder()
                            .user1(currentUser)
                            .user2(targetUser)
                            .createdDatetime(LocalDateTime.now(ZoneOffset.UTC))
                            .lastUpdatedDatetime(LocalDateTime.now(ZoneOffset.UTC))
                            .isActive(true)
                            .build();
                    return chatRepository.save(newChat);
                });

        return createAndSaveMessage(currentUserId, chat, request);
    }

    @Override
    @Transactional
    public DirectMessage sendMessageToChat(Long currentUserId, Long chatId, MessageRequestDTO request) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));
        return createAndSaveMessage(currentUserId, chat, request);
    }

    @Override
    @Transactional
    public void markChatAsRead(Long chatId, Long currentUserId) {
        // Solo marca como leídos los mensajes donde el currentUserId NO es el sender
        messageRepository.markUnreadMessagesAsRead(chatId, currentUserId);
    }

    // Helper method para reutilizar la lógica de creación de mensajes
    private DirectMessage createAndSaveMessage(Long senderId, Chat chat, MessageRequestDTO request) {
        User sender = userRepository.findById(senderId).orElseThrow();

        DirectMessage replyToMessage = null;
        if (request.getReplyToId() != null) {
            replyToMessage = messageRepository.findById(request.getReplyToId()).orElse(null);
        }

        DirectMessage newMessage = DirectMessage.builder()
                .chat(chat)
                .sender(sender)
                .content(request.getContent())
                .link(request.getLink() != null ? request.getLink() : "")
                .replyTo(replyToMessage)
                .isRead(false)
                .createdDatetime(LocalDateTime.now(ZoneOffset.UTC))
                .lastUpdatedDatetime(LocalDateTime.now(ZoneOffset.UTC))
                .isActive(true)
                .build();

        // Actualizamos la fecha del chat para que suba en las listas de "recientes"
        chat.setLastUpdatedDatetime(LocalDateTime.now(ZoneOffset.UTC));
        chatRepository.save(chat);

        return messageRepository.save(newMessage);
    }
}
