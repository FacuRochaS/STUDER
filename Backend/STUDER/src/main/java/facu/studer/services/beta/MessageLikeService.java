package facu.studer.services.beta;

import facu.studer.DTOs.MessageDTO;

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
    MessageDTO like(String username, Long messageId);

    /**
     * Removes a like from a message for the authenticated user.
     *
     * @param username  the authenticated username
     * @param messageId the message ID
     * @return success/error response
     */
    MessageDTO unlike(String username, Long messageId);
}

