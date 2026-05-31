package facu.studer.controllers;

import facu.studer.DTOs.discussions.MessageResponseDTO;
import facu.studer.DTOs.user.FollowRequestDTO;
import facu.studer.DTOs.user.FriendResponseDTO;
import facu.studer.DTOs.user.FriendsListResponseDTO;
import facu.studer.security.SecurityUtils;
import facu.studer.services.FriendService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for friend/follow operations.
 * All endpoints require authentication via JWT token.
 * Users can manage their friend relationships.
 */
@RestController
@RequestMapping("/api/v1/friends")
public class FriendController {

    private final FriendService friendService;
    private final SecurityUtils securityUtils;

    public FriendController(FriendService friendService, SecurityUtils securityUtils) {
        this.friendService = friendService;
        this.securityUtils = securityUtils;
    }

    /**
     * Follow a user or send friend request.
     * Creates a USER type notification.
     *
     * @param request contains the user ID to follow
     * @return friend relationship response
     */
    @PostMapping("/follow")
    public ResponseEntity<FriendResponseDTO> followUser(
            @Valid @RequestBody FollowRequestDTO request) {
        String username = securityUtils.requireCurrentUsername();
        FriendResponseDTO response = friendService.followUser(username, request.getUserId());
        return ResponseEntity.ok(response);
    }

    /**
     * Unfollow a user or cancel friend request.
     * Sets both accept flags to false.
     *
     * @param userId the ID of the user to unfollow
     * @return success message
     */
    @DeleteMapping("/follow/{userId}")
    public ResponseEntity<MessageResponseDTO> unfollowUser(@PathVariable Long userId) {
        String username = securityUtils.requireCurrentUsername();
        boolean success = friendService.unfollowUser(username, userId);
        
        return ResponseEntity.ok(MessageResponseDTO.builder()
                .success(success)
                .message(success ? "friend.unfollow_success" : "friend.not_found")
                .build());
    }

    /**
     * Gets confirmed friends (for initiating chats).
     * Only returns users where both users have accepted the friendship.
     *
     * @param page page number (0-based, default 0)
     * @return paginated friends list
     */
    @GetMapping
    public ResponseEntity<FriendsListResponseDTO> getFriends(
            @RequestParam(defaultValue = "0") int page) {
        String username = securityUtils.requireCurrentUsername();
        FriendsListResponseDTO response = friendService.getFriends(username, page);
        return ResponseEntity.ok(response);
    }
}

