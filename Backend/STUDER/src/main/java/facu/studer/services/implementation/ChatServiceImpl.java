package facu.studer.services.implementation;

import facu.studer.DTOs.chats.ChatSummaryDTO;
import facu.studer.DTOs.chats.LastMessageDTO;
import facu.studer.DTOs.chats.MessageRequestDTO;
import facu.studer.DTOs.chats.MessageResponseDTO;
import facu.studer.DTOs.friends.FriendStatusResponseDTO;
import facu.studer.DTOs.media.ImageUploadResponseDTO;
import facu.studer.DTOs.user.UserPublicResponseDTO;
import facu.studer.entities.User;
import facu.studer.entities.messages.Chat;
import facu.studer.entities.messages.DirectMessage;
import facu.studer.repositories.ChatRepository;
import facu.studer.repositories.DirectMessageRepository;
import facu.studer.repositories.UserRepository;
import facu.studer.services.ChatService;
import facu.studer.services.FriendService;
import facu.studer.services.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatServiceImpl.class);

    private final ChatRepository chatRepository;
    private final DirectMessageRepository messageRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final FriendService friendService;
    private final RestTemplate restTemplate;
    private final String mediaServiceUrl;



    public ChatServiceImpl(ChatRepository chatRepository,
                           DirectMessageRepository messageRepository,
                           UserService userService,
                           UserRepository userRepository,
                           FriendService friendService,
                           RestTemplate restTemplate,
                           @Value("${app.media.service.url}") String mediaServiceUrl) {
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
        this.userService = userService;
        this.userRepository = userRepository;
        this.friendService = friendService;
        this.restTemplate = restTemplate;
        this.mediaServiceUrl = mediaServiceUrl;
    }

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

            // 4. Buscar la relacion
            FriendStatusResponseDTO friendStatus = friendService.getFriendStatus(currentUserId, otherUserId);

            // 5. Armar la respuesta
            return ChatSummaryDTO.builder()
                    .chatId(chat.getId())
                    .otherUser(otherUser)
                    .lastMessage(lastMessageDTO)
                    .friendStatus(friendStatus)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageResponseDTO> getMessagesByChatId(Long chatId, Pageable pageable) {
        Page<DirectMessage> messages = messageRepository.findByChatIdAndIsActiveTrueOrderByCreatedDatetimeDesc(chatId, pageable);
        // Mapeamos la entidad cruda al DTO manteniendo la paginación de Spring
        return messages.map(this::mapToResponseDTO);
    }

    @Override
    @Transactional
    public MessageResponseDTO sendMessageToUser(Long currentUserId, Long targetUserId, MessageRequestDTO request, MultipartFile file) {
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

        DirectMessage savedMessage = createAndSaveMessage(currentUserId, chat, request, file);
        return mapToResponseDTO(savedMessage);
    }

    @Override
    @Transactional
    public MessageResponseDTO sendMessageToChat(Long currentUserId, Long chatId, MessageRequestDTO request, MultipartFile file) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        DirectMessage savedMessage = createAndSaveMessage(currentUserId, chat, request, file);
        return mapToResponseDTO(savedMessage);
    }

    @Override
    @Transactional
    public void markChatAsRead(Long chatId, Long currentUserId) {
        // Solo marca como leídos los mensajes donde el currentUserId NO es el sender
        messageRepository.markUnreadMessagesAsRead(chatId, currentUserId);
    }

    // --- HELPER METHODS ---

    private DirectMessage createAndSaveMessage(Long senderId, Chat chat, MessageRequestDTO request, MultipartFile file) {
        User sender = userRepository.findById(senderId).orElseThrow();

        DirectMessage replyToMessage = null;
        if (request.getReplyToId() != null) {
            replyToMessage = messageRepository.findById(request.getReplyToId()).orElse(null);
        }

        // Subir la imagen y obtener el link
        String mediaLink = uploadMessageMedia(file, sender.getUsername());

        DirectMessage newMessage = DirectMessage.builder()
                .chat(chat)
                .sender(sender)
                .content(request.getContent())
                .link(mediaLink != null ? mediaLink : "")
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

    private String uploadMessageMedia(MultipartFile file, String username) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", file.getResource());

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);


            String url = mediaServiceUrl + "/upload-image/message";
            logger.info("Calling media service at URL: {}", url);


            ImageUploadResponseDTO response = restTemplate.postForObject(url, requestEntity, ImageUploadResponseDTO.class);
            if (response != null && response.getUrls() != null) {
                logger.info("Received URLs from media service for message: {}", response.getUrls());
                return response.getUrls().get("original");
            } else {
                logger.warn("Media service returned a null or empty response for message file.");
            }


        } catch (Exception e) {
            logger.error("🚨 Failed to call media service to upload image for message sent by {}", username, e);
        }
        return null;
    }

    private MessageResponseDTO mapToResponseDTO(DirectMessage message) {
        return MessageResponseDTO.builder()
                .id(message.getId())
                .chatId(message.getChat().getId())
                .senderId(message.getSender().getId())
                .content(message.getContent())
                .link(message.getLink())
                .replyToId(message.getReplyTo() != null ? message.getReplyTo().getId() : null)
                .isRead(message.getIsRead())
                .createdDatetime(message.getCreatedDatetime())
                .build();
    }
}
