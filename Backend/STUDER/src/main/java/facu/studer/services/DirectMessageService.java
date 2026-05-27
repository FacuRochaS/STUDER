package facu.studer.services;

import facu.studer.DTOs.discussions.MessageResponseDTO;
import facu.studer.DTOs.messages.ChatListPageResponseDTO;
import facu.studer.DTOs.messages.DirectMessagePageResponseDTO;
import facu.studer.DTOs.messages.DirectMessageRequestDTO;
import facu.studer.DTOs.messages.DirectMessageResponseDTO;

/**
 * Service interface for DirectMessage operations.
 * Handles direct messaging between users.
 */
public interface DirectMessageService {

    /**
     * Gets paginated messages between current user and another user.
     * Returns the last 30 messages ordered by sent time.
     * Only includes confirmed friends.
     *
     * @param currentUsername the authenticated username
     * @param otherUserId     the ID of the other user
     * @param page           page number (0-based)
     * @return paginated message response
     */
    DirectMessagePageResponseDTO getChatById(String currentUsername, Long otherUserId, int page);

    /**
     * Gets all chats for the current user.
     * Only includes chats with confirmed friends.
     * Shows the last message, unread count, etc.
     *
     * @param currentUsername the authenticated username
     * @param page           page number (0-based)
     * @return paginated chat list response
     */
    ChatListPageResponseDTO getAllChats(String currentUsername, int page);

    /**
     * Gets message requests (messages from non-friends).
     * Shows first message from each non-friend user.
     *
     * @param currentUsername the authenticated username
     * @param page           page number (0-based)
     * @return paginated message requests response
     */
    DirectMessagePageResponseDTO getMessageRequests(String currentUsername, int page);

    /**
     * Sends a direct message to another user.
     * If users are friends, creates a regular message notification.
     * If users are not friends, creates a message request notification.
     *
     * @param currentUsername the authenticated username
     * @param request        the message request
     * @return the sent message response
     */
    DirectMessageResponseDTO sendMessage(String currentUsername, DirectMessageRequestDTO request);

    /**
     * Marks all messages from a specific user as read.
     *
     * @param currentUsername the authenticated username
     * @param senderId       the sender's user ID
     * @return success/error message
     */
    MessageResponseDTO markAsRead(String currentUsername, Long senderId);
}

