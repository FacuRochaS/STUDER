package facu.studer.services;

import facu.studer.DTOs.discussions.MessageResponseDTO;

/**
 * Service interface for user discussion favourite operations.
 * Handles adding and removing discussions from favourites.
 */
public interface UserDiscussionFavService {

    /**
     * Adds a discussion to the user's favourites.
     *
     * @param username     the authenticated username
     * @param discussionId the discussion ID
     * @return success/error response
     */
    MessageResponseDTO addFavourite(String username, Long discussionId);

    /**
     * Removes a discussion from the user's favourites.
     *
     * @param username     the authenticated username
     * @param discussionId the discussion ID
     * @return success/error response
     */
    MessageResponseDTO removeFavourite(String username, Long discussionId);

    /**
     * Checks if a user has favourited a discussion.
     *
     * @param username     the username
     * @param discussionId the discussion ID
     * @return true if favourited
     */
    boolean isFavourite(String username, Long discussionId);
}

