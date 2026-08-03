package facu.studer.services;

import facu.studer.DTOs.discussions.*;
import facu.studer.DTOs.MessageDTO;
import org.springframework.web.multipart.MultipartFile;

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

    DiscussionPageResponseDTO getFavouriteDiscussions(
            String username,
            int page );

    /**
     * Gets user owned discussions.
     *
     * @param username the authenticated username
     * @param page     page number (0-based)
     * @return paginated discussion response
     */
    DiscussionPageResponseDTO getUserOwnDiscussions(String username, int page);

    DiscussionPageResponseDTO getDiscussionsByUsername(String targetUsername, int page);

    DiscussionPageResponseDTO getNewDiscussions(
            String username,
            int page );

    DiscussionPageResponseDTO getPopularDiscussions(
            String username,
            int page );





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
            DiscussionMessageCreateRequestDTO request,
            MultipartFile file);


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


    /**
     * Adds a discussion to the user's favourites.
     *
     * @param username     the authenticated username
     * @param discussionId the discussion ID
     * @return success/error response
     */
    MessageDTO addFavourite(String username, Long discussionId);

    /**
     * Removes a discussion from the user's favourites.
     *
     * @param username     the authenticated username
     * @param discussionId the discussion ID
     * @return success/error response
     */
    MessageDTO removeFavourite(String username, Long discussionId);

    /**
     * Checks if a user has favourited a discussion.
     *
     * @param username     the username
     * @param discussionId the discussion ID
     * @return true if favourited
     */
    boolean isFavourite(String username, Long discussionId);




}
