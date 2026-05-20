package facu.studer.services;

import facu.studer.DTOs.discussions.DiscussionCreateRequestDTO;
import facu.studer.DTOs.discussions.DiscussionPageResponseDTO;
import facu.studer.DTOs.discussions.DiscussionResponseDTO;
import facu.studer.DTOs.discussions.MessageResponseDTO;

import java.util.List;

/**
 * Service interface for discussion operations.
 * Handles listing, creating, and closing public discussions.
 */
public interface DiscussionService {

    /**
     * Gets paginated public discussions with optional filters,
     * ordered by recent activity.
     *
     * @param username       the authenticated username (for favourite/participation info)
     * @param page           page number (0-based)
     * @param tagNames       optional tag name filter
     * @param lastDays       optional created-since filter (last N days)
     * @param activityHours  hours window for activity-based ordering
     * @return paginated discussion response
     */
    DiscussionPageResponseDTO getPublicDiscussions(
            String username,
            int page,
            List<String> tagNames,
            Integer lastDays,
            Integer activityHours);

    /**
     * Gets a single discussion by its ID.
     *
     * @param username the authenticated username (for favourite/participation info)
     * @param discussionId the discussion ID
     * @return the discussion response DTO
     */
    DiscussionResponseDTO getById(String username, Long discussionId);

    /**
     * Gets paginated discussions where the user has participated or favourited.
     *
     * @param username the authenticated username
     * @param page     page number (0-based)
     * @return paginated discussion response
     */
    DiscussionPageResponseDTO getUserDiscussions(String username, int page);

    /**
     * Creates a new public discussion.
     * Tags are resolved internally (created if they don't exist).
     *
     * @param username the authenticated username (owner)
     * @param request  the creation request DTO
     * @return the created discussion response
     */
    DiscussionResponseDTO create(String username, DiscussionCreateRequestDTO request);

    /**
     * Closes a discussion. Only the owner can close it.
     *
     * @param username     the authenticated username
     * @param discussionId the discussion ID
     * @return success/error response
     */
    MessageResponseDTO close(String username, Long discussionId);
}
