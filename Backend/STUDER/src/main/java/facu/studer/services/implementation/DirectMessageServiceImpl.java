package facu.studer.services.implementation;

import facu.studer.DTOs.discussions.MessageResponseDTO;
import facu.studer.DTOs.messages.ChatListPageResponseDTO;
import facu.studer.DTOs.messages.ChatPreviewDTO;
import facu.studer.DTOs.messages.DirectMessagePageResponseDTO;
import facu.studer.DTOs.messages.DirectMessageRequestDTO;
import facu.studer.DTOs.messages.DirectMessageResponseDTO;
import facu.studer.entities.Friend;
import facu.studer.entities.LinkedType;
import facu.studer.entities.User;
import facu.studer.entities.messages.DirectMessage;
import facu.studer.entities.notifications.Notification;
import facu.studer.entities.notifications.UserNotification;
import facu.studer.exceptions.ResourceNotFoundException;
import facu.studer.mappers.DirectMessageMapper;
import facu.studer.repositories.DirectMessageRepository;
import facu.studer.repositories.FriendRepository;
import facu.studer.repositories.NotificationRepository;
import facu.studer.repositories.UserNotificationRepository;
import facu.studer.repositories.UserRepository;
import facu.studer.services.DirectMessageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of DirectMessageService.
 * Handles direct messaging, chat management, and message notifications.
 */
@Service
public class DirectMessageServiceImpl implements DirectMessageService {

    private static final int PAGE_SIZE = 30;
    private static final int CHAT_PAGE_SIZE = 10;

    private final DirectMessageRepository directMessageRepository;
    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final UserNotificationRepository userNotificationRepository;

    public DirectMessageServiceImpl(
            DirectMessageRepository directMessageRepository,
            FriendRepository friendRepository,
            UserRepository userRepository,
            NotificationRepository notificationRepository,
            UserNotificationRepository userNotificationRepository) {
        this.directMessageRepository = directMessageRepository;
        this.friendRepository = friendRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.userNotificationRepository = userNotificationRepository;
    }

    /**
     * Gets chat with another user (last 30 messages, paginated).
     * Only accessible if both users are confirmed friends.
     */
    @Override
    @Transactional(readOnly = true)
    public DirectMessagePageResponseDTO getChatById(String currentUsername, Long otherUserId, int page) {
        User currentUser = userRepository.findByUsername(currentUsername);
        if (currentUser == null) {
            throw new ResourceNotFoundException("user.not_found");
        }

        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new ResourceNotFoundException("user.not_found"));

        // Verify they are friends
        Optional<Friend> friendship = friendRepository.findFriendship(currentUser, otherUser);
        if (friendship.isEmpty() || !friendship.get().getSenderAccept() || !friendship.get().getReceiverAccept()) {
            throw new IllegalArgumentException("chat.users_not_friends");
        }

        // Get messages
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Page<DirectMessage> messagesPage = directMessageRepository
                .findMessagesBetweenUsers(currentUser, otherUser, pageable);

        List<DirectMessageResponseDTO> dtos = messagesPage.getContent().stream()
                .map(DirectMessageMapper::toResponseDTO)
                .collect(Collectors.toList());

        return DirectMessagePageResponseDTO.builder()
                .messages(dtos)
                .totalElements(messagesPage.getTotalElements())
                .hasMore(messagesPage.hasNext())
                .currentPage(page)
                .build();
    }

    /**
     * Gets all chats for the current user (only with confirmed friends).
     * Includes last message, unread count, etc.
     */
    @Override
    @Transactional(readOnly = true)
    public ChatListPageResponseDTO getAllChats(String currentUsername, int page) {
        User currentUser = userRepository.findByUsername(currentUsername);
        if (currentUser == null) {
            throw new ResourceNotFoundException("user.not_found");
        }

        // Get confirmed friends
        List<Friend> confirmedFriendships = friendRepository.findConfirmedFriends(currentUser);

        // Build chat previews
        List<ChatPreviewDTO> chatPreviews = confirmedFriendships.stream()
                .map(friendship -> {
                    User friendUser = friendship.getSender().getId().equals(currentUser.getId()) 
                        ? friendship.getReceiver() 
                        : friendship.getSender();

                    // Get last message
                    Optional<DirectMessage> lastMessage = directMessageRepository
                            .findLastMessageBetweenUsers(currentUser, friendUser);

                    int unreadCount = 0;
                    if (lastMessage.isPresent()) {
                        List<DirectMessage> unreadMessages = directMessageRepository
                                .findByReceiverAndIsReadFalseAndIsActiveTrue(currentUser);
                        unreadCount = (int) unreadMessages.stream()
                                .filter(m -> m.getSender().getId().equals(friendUser.getId()))
                                .count();
                    }

                    return ChatPreviewDTO.builder()
                            .userId(friendUser.getId())
                            .username(friendUser.getUsername())
                            .firstName(friendUser.getFirstName())
                            .lastName(friendUser.getLastName())
                            .lastMessageContent(lastMessage.map(DirectMessage::getContent).orElse(null))
                            .lastMessageTime(lastMessage.map(DirectMessage::getSentAt).orElse(null))
                            .unreadCount(unreadCount)
                            .lastMessageIsFromMe(lastMessage
                                    .map(m -> m.getSender().getId().equals(currentUser.getId()))
                                    .orElse(false))
                            .build();
                })
                .sorted((a, b) -> {
                    if (a.getLastMessageTime() == null) return 1;
                    if (b.getLastMessageTime() == null) return -1;
                    return b.getLastMessageTime().compareTo(a.getLastMessageTime());
                })
                .collect(Collectors.toList());

        // Manual pagination
        int totalElements = chatPreviews.size();
        int pageSize = CHAT_PAGE_SIZE;
        int startIndex = page * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalElements);

        List<ChatPreviewDTO> pageContent = chatPreviews.subList(
                Math.min(startIndex, totalElements),
                endIndex
        );

        return ChatListPageResponseDTO.builder()
                .chats(pageContent)
                .totalElements((long) totalElements)
                .hasMore(endIndex < totalElements)
                .currentPage(page)
                .build();
    }

    /**
     * Gets message requests (messages from non-friends).
     */
    @Override
    @Transactional(readOnly = true)
    public DirectMessagePageResponseDTO getMessageRequests(String currentUsername, int page) {
        User currentUser = userRepository.findByUsername(currentUsername);
        if (currentUser == null) {
            throw new ResourceNotFoundException("user.not_found");
        }

        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Page<DirectMessage> requestsPage = directMessageRepository
                .findMessageRequestsForUser(currentUser, pageable);

        List<DirectMessageResponseDTO> dtos = requestsPage.getContent().stream()
                .map(DirectMessageMapper::toResponseDTO)
                .collect(Collectors.toList());

        return DirectMessagePageResponseDTO.builder()
                .messages(dtos)
                .totalElements(requestsPage.getTotalElements())
                .hasMore(requestsPage.hasNext())
                .currentPage(page)
                .build();
    }

    /**
     * Sends a direct message.
     * Creates a notification based on friendship status:
     * - If friends: regular "new message" notification
     * - If not friends: message request notification
     */
    @Override
    @Transactional
    public DirectMessageResponseDTO sendMessage(String currentUsername, DirectMessageRequestDTO request) {
        User sender = userRepository.findByUsername(currentUsername);
        if (sender == null) {
            throw new ResourceNotFoundException("user.not_found");
        }

        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new ResourceNotFoundException("user.not_found"));

        if (sender.getId().equals(receiver.getId())) {
            throw new IllegalArgumentException("message.cannot_send_to_yourself");
        }

        // Get reply-to message if specified
        DirectMessage replyTo = null;
        if (request.getReplyToId() != null) {
            replyTo = directMessageRepository.findById(request.getReplyToId())
                    .orElseThrow(() -> new ResourceNotFoundException("message.not_found"));
        }

        // Create message
        DirectMessage message = DirectMessage.builder()
                .sender(sender)
                .receiver(receiver)
                .content(request.getContent())
                .link(request.getLink() != null ? request.getLink() : "")
                .sentAt(LocalDateTime.now())
                .replyTo(replyTo)
                .isRead(false)
                .createdDatetime(LocalDateTime.now())
                .lastUpdatedDatetime(LocalDateTime.now())
                .isActive(true)
                .build();

        message = directMessageRepository.save(message);

        // Create notification
        Optional<Friend> friendship = friendRepository.findFriendship(sender, receiver);
        boolean areFriends = friendship.isPresent() && 
                            friendship.get().getSenderAccept() && 
                            friendship.get().getReceiverAccept();

        createMessageNotification(receiver, sender, areFriends);

        return DirectMessageMapper.toResponseDTO(message);
    }

    /**
     * Marks all messages from a sender as read.
     */
    @Override
    @Transactional
    public MessageResponseDTO markAsRead(String currentUsername, Long senderId) {
        User receiver = userRepository.findByUsername(currentUsername);
        if (receiver == null) {
            throw new ResourceNotFoundException("user.not_found");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("user.not_found"));

        List<DirectMessage> unreadMessages = directMessageRepository
                .findByReceiverAndIsReadFalseAndIsActiveTrue(receiver);

        unreadMessages.stream()
                .filter(m -> m.getSender().getId().equals(sender.getId()))
                .forEach(m -> {
                    m.setIsRead(true);
                    m.setLastUpdatedDatetime(LocalDateTime.now());
                });

        directMessageRepository.saveAll(unreadMessages);

        return MessageResponseDTO.builder()
                .success(true)
                .message("message.marked_as_read")
                .build();
    }

    /**
     * Creates a message notification.
     * Type varies based on friendship status.
     */
    private void createMessageNotification(User receiver, User sender, boolean areFriends) {
        String messageKey = areFriends 
            ? "message.new_message" 
            : "message.new_request";

        Notification notification = Notification.builder()
                .title(messageKey + "_title")
                .message(messageKey + "_message")
                .type(LinkedType.USER)
                .linkedId(sender.getId())
                .createdDatetime(LocalDateTime.now())
                .lastUpdatedDatetime(LocalDateTime.now())
                .isActive(true)
                .build();

        notification = notificationRepository.save(notification);

        UserNotification userNotification = UserNotification.builder()
                .user(receiver)
                .notification(notification)
                .read(false)
                .createdDatetime(LocalDateTime.now())
                .lastUpdatedDatetime(LocalDateTime.now())
                .isActive(true)
                .build();

        userNotificationRepository.save(userNotification);
    }
}

