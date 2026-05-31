package facu.studer.services;

import facu.studer.DTOs.user.FriendResponseDTO;
import facu.studer.DTOs.user.FriendsListResponseDTO;

/**
 * Service interface for Friend operations.
 * Handles friend requests, acceptances, and friendship management.
 */
public interface FriendService {

    /**
     * Follow or send friend request to a user.
     * Logic:
     * 1. If receiver has already sent a request, accept it (set both flags to true)
     * 2. If no request exists, create new one with current user as sender
     * 3. If confirmed friendship exists, do nothing
     * Creates a USER type notification.
     *
     * @param currentUsername the authenticated username
     * @param targetUserId    the ID of the user to follow
     * @return the friend relationship response
     */
    FriendResponseDTO followUser(String currentUsername, Long targetUserId);

    /**
     * Unfollow or cancel friend request.
     * Sets both flags to false, effectively canceling the friendship.
     *
     * @param currentUsername the authenticated username
     * @param targetUserId    the ID of the user to unfollow
     * @return success/error message
     */
    Boolean unfollowUser(String currentUsername, Long targetUserId);

    /**
     * Gets confirmed friends (both users have accepted).
     * For initiating direct messages with.
     *
     * @param currentUsername the authenticated username
     * @param page           page number (0-based)
     * @return paginated friends list
     */
    FriendsListResponseDTO getFriends(String currentUsername, int page);
}

