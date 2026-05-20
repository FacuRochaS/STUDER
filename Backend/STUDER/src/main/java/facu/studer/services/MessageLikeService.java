package facu.studer.services;

import facu.studer.DTOs.discussions.MessageResponseDTO;

/**
 * Service interface for message like operations.
 * Handles liking and unliking discussion messages.
 */
public interface MessageLikeService {

    /**
     * Adds a like to a message for the authenticated user.
     *
     * @param username  the authenticated username
     * @param messageId the message ID
     * @return success/error response
     */
    MessageResponseDTO like(String username, Long messageId);

    /**
     * Removes a like from a message for the authenticated user.
     *
     * @param username  the authenticated username
     * @param messageId the message ID
     * @return success/error response
     */
    MessageResponseDTO unlike(String username, Long messageId);
}

