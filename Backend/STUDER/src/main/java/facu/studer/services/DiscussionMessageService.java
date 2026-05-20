package facu.studer.services;

import facu.studer.DTOs.discussions.DiscussionMessageCreateRequestDTO;
import facu.studer.DTOs.discussions.DiscussionMessagePageResponseDTO;
import facu.studer.DTOs.discussions.DiscussionMessageResponseDTO;

/**
 * Service interface for discussion message operations.
 * Handles listing messages with hierarchy and creating new messages.
 */
public interface DiscussionMessageService {

    /**
     * Gets paginated root-level messages for a discussion with hierarchy.
     * Messages are ordered by like count (most liked first).
     * Child messages are nested under their parent.
     *
     * @param discussionId the discussion ID
     * @param username     the authenticated username (for liked-by-user flag)
     * @param page         page number (0-based)
     * @return paginated message response with nested children
     */
    DiscussionMessagePageResponseDTO getMessages(Long discussionId, String username, int page);

    /**
     * Creates a new message in a discussion.
     * Fails if the discussion is closed.
     *
     * @param discussionId the discussion ID
     * @param username     the authenticated username (sender)
     * @param request      the message creation request
     * @return the created message response
     */
    DiscussionMessageResponseDTO createMessage(
            Long discussionId,
            String username,
            DiscussionMessageCreateRequestDTO request);
}

